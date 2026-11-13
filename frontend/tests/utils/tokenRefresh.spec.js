import { beforeEach, describe, expect, it, vi } from 'vitest'

const { axiosPost, authStore, routerPush, ElMessage } = vi.hoisted(() => ({
    axiosPost: vi.fn(),
    authStore: {
        token: '',
        refreshToken: '',
        clearAuth: vi.fn(),
    },
    routerPush: vi.fn(),
    ElMessage: { error: vi.fn() },
}))

vi.mock('axios', () => ({ default: { post: axiosPost } }))
vi.mock('element-plus', () => ({ ElMessage }))
vi.mock('@/stores/modules/auth.js', () => ({ useAuthStore: () => authStore }))
vi.mock('@/router/index.js', () => ({ default: { push: routerPush } }))

const loadModule = async () => {
    vi.resetModules()
    return import('@/utils/tokenRefresh.js')
}

describe('tokenRefresh', () => {
    beforeEach(() => {
        axiosPost.mockReset()
        authStore.token = ''
        authStore.refreshToken = ''
        authStore.clearAuth.mockClear()
        routerPush.mockClear()
        ElMessage.error.mockClear()
    })

    it('刷新成功时更新 accessToken 与 refreshToken', async () => {
        axiosPost.mockResolvedValue({
            data: { code: 200, data: { accessToken: 'new-access', refreshToken: 'new-refresh' } },
        })
        authStore.refreshToken = 'old-refresh'
        const { refreshAccessToken } = await loadModule()

        const token = await refreshAccessToken()

        expect(token).toBe('new-access')
        expect(authStore.token).toBe('new-access')
        expect(authStore.refreshToken).toBe('new-refresh')
        expect(axiosPost).toHaveBeenCalledWith(expect.stringContaining('/auth/refresh?refreshToken=old-refresh'))
    })

    it('刷新接口返回非 200 时抛出异常', async () => {
        axiosPost.mockResolvedValue({ data: { code: 500 } })
        const { refreshAccessToken } = await loadModule()

        await expect(refreshAccessToken()).rejects.toThrow('刷新 token 失败')
    })

    it('缺少 refreshToken 时清理认证并跳转登录', async () => {
        const { handleTokenExpired } = await loadModule()

        await expect(handleTokenExpired({ headers: {} })).rejects.toThrow('登录已过期')
        expect(authStore.clearAuth).toHaveBeenCalled()
        expect(routerPush).toHaveBeenCalledWith('/')
        expect(ElMessage.error).toHaveBeenCalledWith('登录已过期，请重新登录')
    })

    it('刷新接口自身 401 时直接跳转登录', async () => {
        authStore.refreshToken = 'refresh'
        const { handleTokenExpired } = await loadModule()

        await expect(
            handleTokenExpired({ url: '/auth/refresh?refreshToken=x', headers: {} }),
        ).rejects.toThrow('refreshToken 已失效')
        expect(authStore.clearAuth).toHaveBeenCalled()
        expect(ElMessage.error).toHaveBeenCalledWith('refreshToken 已失效，请重新登录')
    })

    it('刷新成功后给原请求补上 Authorization', async () => {
        authStore.refreshToken = 'refresh'
        axiosPost.mockResolvedValue({
            data: { code: 200, data: { accessToken: 'new-access', refreshToken: 'new-refresh' } },
        })
        const { handleTokenExpired } = await loadModule()
        const config = { headers: {} }

        const result = await handleTokenExpired(config)

        expect(result).toBe(config)
        expect(config.headers.Authorization).toBe('Bearer new-access')
    })

    it('刷新进行中时并发请求进入队列并共享新 token', async () => {
        authStore.refreshToken = 'refresh'
        let resolvePost
        axiosPost.mockReturnValue(
            new Promise((resolve) => {
                resolvePost = resolve
            }),
        )
        const { handleTokenExpired } = await loadModule()

        const first = handleTokenExpired({ headers: {} })
        const config2 = { headers: {} }
        const second = handleTokenExpired(config2)

        resolvePost({
            data: { code: 200, data: { accessToken: 'queued-token', refreshToken: 'queued-refresh' } },
        })

        const firstConfig = await first
        const secondConfig = await second

        expect(firstConfig.headers.Authorization).toBe('Bearer queued-token')
        expect(secondConfig).toBe(config2)
        expect(config2.headers.Authorization).toBe('Bearer queued-token')
        expect(axiosPost).toHaveBeenCalledTimes(1)
    })

    it('刷新失败时清空队列并跳转登录', async () => {
        authStore.refreshToken = 'refresh'
        axiosPost.mockRejectedValue(new Error('network'))
        const { handleTokenExpired } = await loadModule()

        await expect(handleTokenExpired({ headers: {} })).rejects.toThrow('刷新 token 失败')
        expect(authStore.clearAuth).toHaveBeenCalled()
        expect(routerPush).toHaveBeenCalledWith('/')
    })
})
