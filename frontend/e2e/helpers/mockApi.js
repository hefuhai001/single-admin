/**
 * E2E 接口桩：拦截浏览器发出的 /api 请求，返回与后端 SaResult 一致的结构。
 * 让登录、菜单渲染、按钮级权限三条主流程无需真实后端即可稳定验证。
 */

/** 管理员可用的全部权限码 */
export const ADMIN_PERMISSIONS = [
    'user:query',
    'user:add',
    'user:update',
    'user:delete',
    'role:query',
    'role:add',
    'role:update',
    'role:delete',
]

/** 仅具备查询权限的受限账号 */
export const LIMITED_PERMISSIONS = ['user:query']

/** 动态路由（与后端 /admin/user/routes 返回结构一致） */
export const DYNAMIC_ROUTES = [
    {
        path: '/admin/dashboard/index',
        name: 'dashboardHome',
        component: '/admin/dashboard/index',
        meta: { title: '首页', icon: 'Platform' },
    },
    {
        path: '/admin/system',
        name: 'system',
        component: '',
        meta: { title: '系统管理', icon: 'Setting' },
        children: [
            {
                path: '/admin/system/user',
                name: 'systemUser',
                component: '/admin/system/user/index',
                meta: { title: '用户管理', icon: 'User' },
            },
        ],
    },
]

const CONFIG_LIST = [
    { configKey: 'site.title', configValue: 'Single Admin', status: true },
    { configKey: 'register.enable', configValue: 'false', status: true },
    { configKey: 'site.use_backend_theme_color', configValue: 'false', status: true },
]

const ok = (data) => ({ code: 200, msg: 'ok', data })

const USER_PAGE = {
    records: [
        {
            id: 1,
            username: 'admin',
            nickname: '管理员',
            email: 'admin@example.com',
            status: 1,
            createdAt: '2026-01-01 00:00:00',
            updatedAt: '2026-01-01 00:00:00',
        },
    ],
    total: 1,
}

/**
 * 为页面注册接口桩。
 *
 * @param {import('@playwright/test').Page} page 页面对象
 * @param {{permissions?: string[], routes?: object[]}} [options] 覆盖默认权限与路由
 */
export async function mockApi(page, options = {}) {
    const permissions = options.permissions || ADMIN_PERMISSIONS
    const routes = options.routes || DYNAMIC_ROUTES

    await page.route('**/api/**', async (route) => {
        const request = route.request()
        const url = new URL(request.url())
        const path = url.pathname.replace(/^\/api/, '')
        const method = request.method()

        const json = (body) =>
            route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify(body),
            })

        if (path === '/auth/login' && method === 'POST') {
            return json(ok({ accessToken: 'e2e-access-token', refreshToken: 'e2e-refresh-token' }))
        }
        if (path === '/auth/profile') {
            return json(ok({ id: 1, username: 'admin', nickname: '管理员', roles: ['admin'] }))
        }
        if (path === '/system/config/list') {
            return json(ok(CONFIG_LIST))
        }
        if (path === '/admin/user/routes') {
            return json(ok(routes))
        }
        if (path === '/admin/user/permissions') {
            return json(ok(permissions))
        }
        if (path === '/admin/user/page') {
            return json(ok(USER_PAGE))
        }

        return json(ok(null))
    })
}
