import { beforeEach, describe, expect, it, vi } from 'vitest'

const { routerMock, loadDynamicRoutes, resetDynamicRoutes, nprogress } = vi.hoisted(() => {
    const routerMock = {
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
        addRoute: vi.fn(),
        getRoutes: vi.fn(() => []),
        removeRoute: vi.fn(),
        hasRoute: vi.fn(() => false),
        options: null,
    }
    return {
        routerMock,
        loadDynamicRoutes: vi.fn(),
        resetDynamicRoutes: vi.fn(),
        nprogress: { configure: vi.fn(), start: vi.fn(), done: vi.fn() },
    }
})

vi.mock('vue-router', () => ({
    createRouter: vi.fn((options) => {
        routerMock.options = options
        return routerMock
    }),
    createWebHistory: vi.fn(() => ({})),
}))
vi.mock('@/router/modules/dynamicRouter.js', () => ({ loadDynamicRoutes, resetDynamicRoutes }))
vi.mock('@/router/modules/staticRouter.js', () => ({ staticRoutes: [], errorRoutes: [] }))
vi.mock('nprogress', () => ({ default: nprogress }))
vi.mock('nprogress/nprogress.css', () => ({}))

const loadRouterModule = async () => {
    vi.resetModules()
    const module = await import('@/router/index.js')
    const guard = routerMock.beforeEach.mock.calls.at(-1)[0]
    const afterEach = routerMock.afterEach.mock.calls.at(-1)[0]
    return { ...module, guard, afterEach }
}

describe('router index', () => {
    beforeEach(() => {
        routerMock.beforeEach.mockClear()
        routerMock.afterEach.mockClear()
        loadDynamicRoutes.mockReset().mockResolvedValue()
        resetDynamicRoutes.mockClear()
        nprogress.configure.mockClear()
        nprogress.start.mockClear()
        nprogress.done.mockClear()
    })

    it('创建路由时配置 NProgress 与 scrollBehavior', async () => {
        const router = await loadRouterModule()
        const options = router.default.options

        expect(nprogress.configure).toHaveBeenCalled()
        expect(options.scrollBehavior()).toEqual({ left: 0, top: 0 })
        expect(router.default).toBe(routerMock)
    })

    it('白名单路径直接放行', async () => {
        const { guard } = await loadRouterModule()
        const next = vi.fn()

        await guard({ path: '/register', name: 'register' }, {}, next)
        expect(next).toHaveBeenCalledWith()
        expect(loadDynamicRoutes).not.toHaveBeenCalled()

        await guard({ path: '/anything', name: 'auth' }, {}, next)
        expect(next).toHaveBeenCalledTimes(2)
        expect(loadDynamicRoutes).not.toHaveBeenCalled()
    })

    it('首次访问业务路由时加载动态路由并重新导航', async () => {
        const { guard } = await loadRouterModule()
        const next = vi.fn()

        await guard({ path: '/admin/user', fullPath: '/admin/user', query: { page: 2 } }, {}, next)

        expect(nprogress.start).toHaveBeenCalled()
        expect(loadDynamicRoutes).toHaveBeenCalledWith(routerMock)
        expect(next).toHaveBeenCalledWith({
            path: '/admin/user',
            query: { page: 2 },
            replace: true,
        })
    })

    it('动态路由已加载后直接放行', async () => {
        const { guard } = await loadRouterModule()
        const next = vi.fn()
        await guard({ path: '/admin/user', query: {} }, {}, next)

        next.mockClear()
        await guard({ path: '/admin/role', query: {} }, {}, next)

        expect(loadDynamicRoutes).toHaveBeenCalledTimes(1)
        expect(next).toHaveBeenCalledWith()
    })

    it('动态路由加载失败时回退到登录页', async () => {
        loadDynamicRoutes.mockRejectedValue(new Error('load-error'))
        const { guard } = await loadRouterModule()
        const next = vi.fn()

        await guard({ path: '/admin/user', query: {} }, {}, next)

        expect(next).toHaveBeenCalledWith('/')
    })

    it('afterEach 结束进度条', async () => {
        const { afterEach } = await loadRouterModule()

        afterEach()

        expect(nprogress.done).toHaveBeenCalled()
    })

    it('resetRouterState 清理动态路由并重置加载标志', async () => {
        const { guard, resetRouterState } = await loadRouterModule()
        const next = vi.fn()
        await guard({ path: '/admin/user', query: {} }, {}, next)

        resetRouterState()
        expect(resetDynamicRoutes).toHaveBeenCalledWith(routerMock)

        loadDynamicRoutes.mockClear()
        await guard({ path: '/admin/user', query: {} }, {}, next)
        expect(loadDynamicRoutes).toHaveBeenCalledTimes(1)
    })
})
