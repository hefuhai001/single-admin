import { useRouterStore } from '@/stores/modules/router.js'

const modules = import.meta.glob('@/views/**/*.vue')

/**
 * 根据组件路径查找对应的模块
 * Vite 的 import.meta.glob 返回的键格式可能为 /src/views/... 或 ./src/views/...
 * 这里同时尝试两种格式
 */
function findModule(componentPath) {
    let path = componentPath
    if (path.startsWith('/')) {
        path = path.substring(1)
    }

    const candidates = [`/src/views/${path}.vue`, `./src/views/${path}.vue`]

    for (const key of candidates) {
        if (modules[key]) {
            return modules[key]
        }
    }

    // 兜底：遍历所有键，通过后缀匹配
    const allKeys = Object.keys(modules)
    const matchedKey = allKeys.find(
        (key) => key.replace(/^\.\//, '').replace(/^\//, '') === `src/views/${path}.vue`,
    )

    return matchedKey ? modules[matchedKey] : undefined
}

export function transformRoutes(routes) {
    return routes.map((route) => {
        const routeConfig = {
            path: route.path,
            name: route.name,
            meta: route.meta
                ? {
                      title: route.meta.title,
                      icon: route.meta.icon,
                      hidden: route.meta.hidden,
                      keepAlive: route.meta.keepAlive,
                      permissions: route.meta.permissions,
                  }
                : {},
        }

        if (route.component && route.component.trim() !== '') {
            const component = findModule(route.component)
            if (component) {
                routeConfig.component = component
            }
        }

        if (route.redirect) {
            routeConfig.redirect = route.redirect
        }

        if (route.children && route.children.length > 0) {
            routeConfig.children = transformRoutes(route.children)
        }

        return routeConfig
    })
}

export async function loadDynamicRoutes(router) {
    const routerStore = useRouterStore()
    try {
        // 并行加载路由和权限，减少等待时间
        await Promise.all([routerStore.initDynamicRoutes(), routerStore.initPermissions()])

        const dynamicRoutes = transformRoutes(routerStore.getDynamicRoutes())
        dynamicRoutes.forEach((route) => {
            router.addRoute('admin', route)
        })

        // 动态路由加载完成后，再添加 404 catch-all 路由
        // 确保 404 路由的优先级最低，不会提前捕获动态路由
        router.addRoute({
            path: '/:pathMatch(.*)*',
            name: 'NotFound',
            component: () => import('@/components/error/404.vue'),
            meta: {
                title: '404页面',
            },
        })
    } catch (error) {
        return Promise.reject(error)
    }
}

/**
 * 清除动态路由，退出登录时调用
 * 需要移除 admin 下的动态子路由和 404 catch-all 路由
 */
export function resetDynamicRoutes(router) {
    const routerStore = useRouterStore()

    // 获取 admin 下的所有子路由名称并移除
    const adminRoute = router.getRoutes().find((r) => r.name === 'admin')
    if (adminRoute && adminRoute.children) {
        adminRoute.children.forEach((child) => {
            if (child.name) {
                router.removeRoute(child.name)
            }
        })
    }

    // 移除 404 catch-all 路由
    if (router.hasRoute('NotFound')) {
        router.removeRoute('NotFound')
    }

    // 清空 store 中的路由和权限数据
    routerStore.setDynamicRoutes([])
    routerStore.setPermissions([])
}
