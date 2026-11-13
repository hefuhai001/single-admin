import { beforeEach, describe, expect, it, vi } from 'vitest'

const { routerStore, addRoute, removeRoute, hasRoute, getRoutes } = vi.hoisted(() => ({
    routerStore: {
        initDynamicRoutes: vi.fn(),
        initPermissions: vi.fn(),
        getDynamicRoutes: vi.fn(() => []),
        setDynamicRoutes: vi.fn(),
        setPermissions: vi.fn(),
    },
    addRoute: vi.fn(),
    removeRoute: vi.fn(),
    hasRoute: vi.fn(() => false),
    getRoutes: vi.fn(() => []),
}))

vi.mock('@/stores/modules/router.js', () => ({ useRouterStore: () => routerStore }))

import { transformRoutes, loadDynamicRoutes, resetDynamicRoutes } from '@/router/modules/dynamicRouter.js'
import { staticRoutes, errorRoutes } from '@/router/modules/staticRouter.js'

describe('dynamicRouter.transformRoutes', () => {
    it('只保留标准 meta 字段并解析已知组件', () => {
        const routes = transformRoutes([
            {
                path: '/admin/user',
                name: 'User',
                component: '/admin/system/user/index.vue',
                redirect: '/admin/user/list',
                meta: {
                    title: '用户管理',
                    icon: 'User',
                    hidden: true,
                    keepAlive: true,
                    permissions: ['user:query'],
                    extra: 'ignored',
                },
            },
        ])

        expect(routes[0]).toEqual(
            expect.objectContaining({
                path: '/admin/user',
                name: 'User',
                redirect: '/admin/user/list',
                meta: {
                    title: '用户管理',
                    icon: 'User',
                    hidden: true,
                    keepAlive: true,
                    permissions: ['user:query'],
                },
            }),
        )
        expect(routes[0].component).toBeTruthy()
    })

    it('组件路径支持去空格、去前导斜杠与 .vue 后缀', () => {
        const routes = transformRoutes([
            { path: '/a', name: 'A', component: '  admin/dashboard/index.vue  ' },
        ])

        expect(routes[0].component).toBeTruthy()
    })

    it('未知或空组件不设置 component', () => {
        const routes = transformRoutes([
            { path: '/a', name: 'A', component: '' },
            { path: '/b', name: 'B', component: '   ' },
            { path: '/c', name: 'C', component: 'not/exist.vue' },
            { path: '/d', name: 'D' },
        ])

        routes.forEach((route) => expect(route.component).toBeUndefined())
    })

    it('无 meta 时使用空对象并递归转换 children', () => {
        const routes = transformRoutes([
            {
                path: '/parent',
                name: 'Parent',
                children: [{ path: '/child', name: 'Child', meta: { title: '子级' } }],
            },
        ])

        expect(routes[0].meta).toEqual({})
        expect(routes[0].children[0].meta).toEqual({ title: '子级' })
    })

    it('children 为空数组时不生成 children 字段', () => {
        const routes = transformRoutes([{ path: '/a', name: 'A', children: [] }])

        expect(routes[0].children).toBeUndefined()
    })
})

describe('dynamicRouter.loadDynamicRoutes', () => {
    const router = { addRoute }

    beforeEach(() => {
        addRoute.mockClear()
        routerStore.initDynamicRoutes.mockReset().mockResolvedValue()
        routerStore.initPermissions.mockReset().mockResolvedValue()
        routerStore.getDynamicRoutes.mockReset().mockReturnValue([])
    })

    it('加载路由与权限并注册动态路由和 404', async () => {
        routerStore.getDynamicRoutes.mockReturnValue([
            { path: '/admin/user', name: 'User', component: '/admin/system/user/index.vue' },
        ])

        await loadDynamicRoutes(router)

        expect(routerStore.initDynamicRoutes).toHaveBeenCalled()
        expect(routerStore.initPermissions).toHaveBeenCalled()
        expect(addRoute).toHaveBeenCalledWith('admin', expect.objectContaining({ name: 'User' }))
        expect(addRoute).toHaveBeenCalledWith(
            expect.objectContaining({ path: '/:pathMatch(.*)*', name: 'NotFound' }),
        )
    })

    it('加载失败时 reject', async () => {
        routerStore.initDynamicRoutes.mockRejectedValue(new Error('routes-error'))

        await expect(loadDynamicRoutes(router)).rejects.toThrow('routes-error')
    })
})

describe('dynamicRouter.resetDynamicRoutes', () => {
    const router = {
        getRoutes,
        removeRoute,
        hasRoute,
    }

    beforeEach(() => {
        getRoutes.mockReset().mockReturnValue([])
        removeRoute.mockClear()
        hasRoute.mockReset().mockReturnValue(false)
        routerStore.setDynamicRoutes.mockClear()
        routerStore.setPermissions.mockClear()
    })

    it('移除 admin 子路由与 NotFound 并清空 store', () => {
        getRoutes.mockReturnValue([
            { name: 'admin', children: [{ name: 'User' }, { name: 'Role' }, {}] },
        ])
        hasRoute.mockReturnValue(true)

        resetDynamicRoutes(router)

        expect(removeRoute).toHaveBeenCalledWith('User')
        expect(removeRoute).toHaveBeenCalledWith('Role')
        expect(removeRoute).toHaveBeenCalledWith('NotFound')
        expect(routerStore.setDynamicRoutes).toHaveBeenCalledWith([])
        expect(routerStore.setPermissions).toHaveBeenCalledWith([])
    })

    it('无 admin 路由或无 NotFound 时也能安全执行', () => {
        resetDynamicRoutes(router)

        expect(removeRoute).not.toHaveBeenCalled()
        expect(routerStore.setDynamicRoutes).toHaveBeenCalledWith([])
    })
})

describe('staticRouter', () => {
    it('包含登录、注册、后台与 403 路由', () => {
        expect(staticRoutes.map((route) => route.path)).toEqual(['/', '/register', '/admin'])
        expect(errorRoutes.some((route) => route.path === '/403')).toBe(true)
    })

    it('路由组件为可调用的懒加载函数', () => {
        ;[...staticRoutes, ...errorRoutes].forEach((route) => {
            expect(typeof route.component).toBe('function')
        })
    })
})
