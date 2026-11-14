import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.unmock('@/utils/requestUtil.js')

const { requestInstance, requestUse, responseUse, authStore, handleTokenExpired, ElMessage } = vi.hoisted(
    () => {
        const requestInstance = vi.fn(() => Promise.resolve('retried-response'))
        requestInstance.interceptors = {
            request: { use: vi.fn() },
            response: { use: vi.fn() },
        }
        const requestUse = vi.fn()
        const responseUse = vi.fn()
        requestInstance.interceptors.request.use = requestUse
        requestInstance.interceptors.response.use = responseUse
        return {
            requestInstance,
            requestUse,
            responseUse,
            authStore: { token: '', deviceId: '' },
            handleTokenExpired: vi.fn(() => Promise.resolve({ url: '/retry' })),
            ElMessage: { error: vi.fn() },
        }
    },
)

vi.mock('axios', () => ({
    default: {
        create: vi.fn(() => requestInstance),
    },
}))

vi.mock('element-plus', () => ({ ElMessage }))
vi.mock('@/stores/modules/auth.js', () => ({ useAuthStore: () => authStore }))
vi.mock('@/utils/tokenRefresh.js', () => ({ handleTokenExpired }))

const loadModule = async () => {
    vi.resetModules()
    const module = await import('@/utils/requestUtil.js')
    return {
        request: module.default,
        requestInterceptor: requestUse.mock.calls.at(-1)[0],
        responseSuccess: responseUse.mock.calls.at(-1)[0],
        responseError: responseUse.mock.calls.at(-1)[1],
    }
}

describe('requestUtil', () => {
    beforeEach(() => {
        requestInstance.mockClear()
        requestUse.mockClear()
        responseUse.mockClear()
        handleTokenExpired.mockClear()
        ElMessage.error.mockClear()
        authStore.token = ''
        authStore.deviceId = ''
    })

    it('请求拦截器在登录后注入 Authorization 与设备标识', async () => {
        const { requestInterceptor } = await loadModule()
        authStore.token = 'token-1'
        authStore.deviceId = 'device-1'

        const config = { headers: {} }
        const result = requestInterceptor(config)

        expect(result).toBe(config)
        expect(config.headers.Authorization).toBe('Bearer token-1')
        expect(config.headers['X-Device-Id']).toBe('device-1')
        expect(config.headers.Accept).toContain('application/json')
    })

    it('未登录时只注入 Accept 头', async () => {
        const { requestInterceptor } = await loadModule()
        const config = { headers: {} }

        requestInterceptor(config)

        expect(config.headers.Authorization).toBeUndefined()
        expect(config.headers.Accept).toContain('text/plain')
    })

    it('blob 响应直接返回原始 response', async () => {
        const { responseSuccess } = await loadModule()
        const response = { config: { responseType: 'blob' }, data: new Blob() }

        expect(responseSuccess(response)).toBe(response)
    })

    it('空响应体直接返回', async () => {
        const { responseSuccess } = await loadModule()
        const response = { config: {}, data: null }

        expect(responseSuccess(response)).toBe(response)
    })

    it('业务 code 401 时刷新 token 并重试请求', async () => {
        const { responseSuccess } = await loadModule()
        const response = { config: { url: '/admin/user/page' }, data: { code: 401 } }

        const result = await responseSuccess(response)

        expect(handleTokenExpired).toHaveBeenCalledWith(response.config)
        expect(requestInstance).toHaveBeenCalledWith({ url: '/retry' })
        expect(result).toBe('retried-response')
    })

    it('业务 code 非 200 时提示并 reject', async () => {
        const { responseSuccess } = await loadModule()
        const response = { config: {}, data: { code: 500, msg: '服务异常' } }

        await expect(responseSuccess(response)).rejects.toThrow('服务异常')
        expect(ElMessage.error).toHaveBeenCalledWith('服务异常')
    })

    it('业务 code 非 200 且无 msg 时使用默认提示', async () => {
        const { responseSuccess } = await loadModule()

        await expect(responseSuccess({ config: {}, data: { code: 500 } })).rejects.toThrow('请求失败')
        expect(ElMessage.error).toHaveBeenCalledWith('请求失败')
    })

    it('HTTP 401 时刷新 token 并重试请求', async () => {
        const { responseError } = await loadModule()
        const config = { url: '/admin/user/page' }
        const error = { response: { status: 401, config } }

        const result = await responseError(error)

        expect(handleTokenExpired).toHaveBeenCalledWith(config)
        expect(result).toBe('retried-response')
    })

    it('HTTP 404 时提示且继续 reject', async () => {
        const { responseError } = await loadModule()
        const error = { response: { status: 404 }, config: {} }

        await expect(responseError(error)).rejects.toBe(error)
        expect(ElMessage.error).toHaveBeenCalledWith(
            expect.objectContaining({ message: '404 not found' }),
        )
    })

    it('HTTP 403 时展示响应体中的权限提示', async () => {
        const { responseError } = await loadModule()
        const error = { response: { status: 403, data: { code: 403, msg: '缺少权限：user:add' } } }

        await expect(responseError(error)).rejects.toBe(error)
        expect(ElMessage.error).toHaveBeenCalledWith('缺少权限：user:add')
    })

    it('HTTP 500 且无响应体时使用默认提示', async () => {
        const { responseError } = await loadModule()
        const error = { response: { status: 500 } }

        await expect(responseError(error)).rejects.toBe(error)
        expect(ElMessage.error).toHaveBeenCalledWith(expect.any(String))
    })

    it('其他异常直接 reject', async () => {
        const { responseError } = await loadModule()
        const error = new Error('network')

        await expect(responseError(error)).rejects.toBe(error)
        expect(ElMessage.error).not.toHaveBeenCalled()
    })
})
