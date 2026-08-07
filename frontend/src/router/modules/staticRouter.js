export const staticRoutes = [
    {
        path: '/',
        name: 'login',
        component: () => import('@/views/login/index.vue'),
    },
    {
        path: '/register',
        name: 'register',
        component: () => import('@/views/register/index.vue'),
    },
    {
        path: '/admin',
        name: 'admin',
        component: () => import('@/views/admin/layout/index.vue'),
        meta: {
            title: '仪表盘',
        },
        redirect: '/admin/dashboard/index',
        children: [],
    },
]

export const errorRoutes = [
    {
        path: '/403',
        name: '403',
        component: () => import('@/components/error/403.vue'),
        meta: {
            title: '403页面',
        },
    },
    // 404 catch-all 路由在动态路由加载完成后通过 addRoute 动态添加
    // 见 dynamicRouter.js 中的 loadDynamicRoutes 函数
]
