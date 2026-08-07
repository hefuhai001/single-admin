import { createRouter, createWebHistory } from 'vue-router'
import { loadDynamicRoutes, resetDynamicRoutes } from './modules/dynamicRouter.js'
import { staticRoutes, errorRoutes } from './modules/staticRouter.js'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({
    easing: 'ease',
    speed: 500,
    showSpinner: false,
    trickleSpeed: 200,
    minimum: 0.3,
})

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [...staticRoutes, ...errorRoutes],
    strict: false,
    scrollBehavior: () => ({ left: 0, top: 0 }),
})

// 标记动态路由是否已加载，防止并发请求和重复加载
let isDynamicRoutesLoaded = false

router.beforeEach(async (to, from, next) => {
    NProgress.start()

    // 1. 白名单直接放行
    const whiteList = ['/', '/register', '/auth', '/404']
    if (whiteList.includes(to.path) || to.name === 'auth') {
        return next()
    }

    // 2. 动态路由已加载，直接放行
    if (isDynamicRoutesLoaded) {
        return next()
    }

    // 3. 动态路由未加载，需要加载
    try {
        await loadDynamicRoutes(router)
        isDynamicRoutesLoaded = true

        // 使用 to.fullPath 重新导航，让 Vue Router 重新匹配新添加的动态路由
        // 注意：不能传 { ...to }，因为 to 对象中包含 name: undefined 等旧导航状态
        // 会导致 Vue Router 无法正确匹配新添加的路由
        next({ path: to.fullPath, query: to.query, replace: true })
    } catch (error) {
        console.error('加载动态路由失败:', error)
        isDynamicRoutesLoaded = true
        next('/')
    }
})

router.afterEach(() => {
    NProgress.done()
})

/**
 * 重置路由状态，退出登录时调用
 * 清除动态路由 + 重置加载标志，确保重新登录时路由能正确加载
 */
export function resetRouterState() {
    resetDynamicRoutes(router)
    isDynamicRoutesLoaded = false
}

export default router
