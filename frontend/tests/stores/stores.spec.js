import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

import { apiMock } from '../helpers/apiMock.js'

const { ElMessage, resetRouterState } = vi.hoisted(() => {
    const ElMessage = Object.assign(vi.fn(), { success: vi.fn(), error: vi.fn() })
    return { ElMessage, resetRouterState: vi.fn() }
})

vi.mock('element-plus', () => ({ ElMessage }))
vi.mock('@/router/index.js', () => ({ resetRouterState }))

import { useAuthStore } from '@/stores/modules/auth.js'
import { useConfigStore } from '@/stores/modules/config.js'
import { useRouterStore } from '@/stores/modules/router.js'

describe('auth store', () => {
    beforeEach(() => {
        setActivePinia(createPinia())
        localStorage.clear()
        ElMessage.mockClear()
        ElMessage.success.mockClear()
        resetRouterState.mockClear()
    })

    it('登录成功生成 deviceId 并拉取用户信息', async () => {
        const store = useAuthStore()

        const result = await store.login({ username: 'admin', password: '123456' })

        expect(result).toBe(true)
        expect(store.token).toBe('access-token')
        expect(store.refreshToken).toBe('refresh-token')
        expect(store.userInfo.username).toBe('admin')
        expect(localStorage.getItem('deviceId')).toBeTruthy()
        expect(apiMock.auth.login).toHaveBeenCalledWith(
            'admin',
            '123456',
            localStorage.getItem('deviceId'),
        )
        expect(ElMessage).toHaveBeenCalledWith({ type: 'success', message: 'ok' })
    })

    it('已存在 deviceId 时复用本地标识', async () => {
        localStorage.setItem('deviceId', 'existing-device')
        const store = useAuthStore()

        await store.login({ username: 'admin', password: '123456' })

        expect(apiMock.auth.login).toHaveBeenCalledWith('admin', '123456', 'existing-device')
    })

    it('登录响应无数据时返回 false', async () => {
        apiMock.auth.login.mockResolvedValueOnce({ data: { code: 500, msg: '失败' } })
        const store = useAuthStore()

        expect(await store.login({ username: 'a', password: 'b' })).toBe(false)
    })

    it('登录接口异常时向上抛出', async () => {
        apiMock.auth.login.mockRejectedValueOnce(new Error('network'))
        const store = useAuthStore()

        await expect(store.login({ username: 'a', password: 'b' })).rejects.toThrow('network')
    })

    it('注册成功保存 token 并拉取用户信息', async () => {
        const store = useAuthStore()

        const result = await store.register({ username: 'new-user', password: '123456' })

        expect(result).toBe(true)
        expect(store.token).toBe('access-token')
        expect(ElMessage.success).toHaveBeenCalledWith('注册成功')
        expect(apiMock.auth.register).toHaveBeenCalledWith(
            expect.objectContaining({ username: 'new-user', deviceId: expect.any(String) }),
        )
    })

    it('注册响应无数据时返回 false', async () => {
        apiMock.auth.register.mockResolvedValueOnce({ data: { code: 500 } })
        const store = useAuthStore()

        expect(await store.register({ username: 'a' })).toBe(false)
    })

    it('注册接口异常时向上抛出', async () => {
        apiMock.auth.register.mockRejectedValueOnce(new Error('register-error'))
        const store = useAuthStore()

        await expect(store.register({ username: 'a' })).rejects.toThrow('register-error')
    })

    it('退出登录无论接口是否成功都会清理认证状态', async () => {
        const store = useAuthStore()
        store.token = 't'
        store.refreshToken = 'r'
        store.userInfo = { username: 'admin' }

        await store.logout()

        expect(store.token).toBe('')
        expect(store.refreshToken).toBe('')
        expect(store.userInfo).toEqual({})
        expect(resetRouterState).toHaveBeenCalled()
    })

    it('退出登录接口失败也会清理认证状态', async () => {
        apiMock.auth.logout.mockRejectedValueOnce(new Error('logout-error'))
        const store = useAuthStore()

        await store.logout()

        expect(store.token).toBe('')
        expect(resetRouterState).toHaveBeenCalled()
    })

    it('getProfile 无数据时返回 null', async () => {
        apiMock.auth.profile.mockResolvedValueOnce({ data: { code: 200 } })
        const store = useAuthStore()

        expect(await store.getProfile()).toBeNull()
    })

    it('getProfile 接口异常时向上抛出', async () => {
        apiMock.auth.profile.mockRejectedValueOnce(new Error('profile-error'))
        const store = useAuthStore()

        await expect(store.getProfile()).rejects.toThrow('profile-error')
    })

    it('setUserInfo/getUserInfo/clearAuth 正常工作', () => {
        const store = useAuthStore()

        store.setUserInfo({ username: 'demo' })
        expect(store.getUserInfo()).toEqual({ username: 'demo' })

        store.token = 't'
        store.refreshToken = 'r'
        store.clearAuth()

        expect(store.token).toBe('')
        expect(store.refreshToken).toBe('')
        expect(store.userInfo).toEqual({})
        expect(resetRouterState).toHaveBeenCalled()
    })
})

describe('config store', () => {
    beforeEach(() => {
        setActivePinia(createPinia())
        ElMessage.error.mockClear()
    })

    it('loadConfig 加载并建立配置映射', async () => {
        const store = useConfigStore()

        const result = await store.loadConfig()

        expect(result.length).toBe(5)
        expect(store.loading).toBe(false)
        expect(store.configMap['site.title'].configValue).toBe('Single Admin')
    })

    it('loadConfig 遇到非标准响应返回空数组', async () => {
        apiMock.config.listConfig.mockResolvedValueOnce({ data: { code: 500, data: null } })
        const store = useConfigStore()

        expect(await store.loadConfig()).toEqual([])
    })

    it('loadConfig 失败时提示并抛出', async () => {
        apiMock.config.listConfig.mockRejectedValueOnce(new Error('config-error'))
        const store = useConfigStore()

        await expect(store.loadConfig()).rejects.toThrow('config-error')
        expect(ElMessage.error).toHaveBeenCalledWith('加载系统配置失败')
        expect(store.loading).toBe(false)
    })

    it('getConfig/getValue 根据状态与默认值返回', async () => {
        const store = useConfigStore()
        store.configList = [
            { configKey: 'a', configValue: 'A', status: true },
            { configKey: 'b', configValue: 'B', status: false },
            { configKey: 'c', configValue: '', status: true },
        ]

        expect(store.getConfig('a')).toEqual(expect.objectContaining({ configValue: 'A' }))
        expect(store.getConfig('missing')).toBeNull()
        expect(store.getValue('a')).toBe('A')
        expect(store.getValue('b', 'fallback')).toBe('fallback')
        expect(store.getValue('c', 'fallback')).toBe('fallback')
        expect(store.getValue('missing', 'fallback')).toBe('fallback')
    })

    it('isEnabled 支持 true/1/yes/on 与非法配置', () => {
        const store = useConfigStore()
        store.configList = [
            { configKey: 't', configValue: 'true', status: true },
            { configKey: 'one', configValue: 1, status: true },
            { configKey: 'yes', configValue: 'YES', status: true },
            { configKey: 'on', configValue: 'on', status: true },
            { configKey: 'off', configValue: 'false', status: true },
            { configKey: 'disabled', configValue: 'true', status: false },
        ]

        expect(store.isEnabled('t')).toBe(true)
        expect(store.isEnabled('one')).toBe(true)
        expect(store.isEnabled('yes')).toBe(true)
        expect(store.isEnabled('on')).toBe(true)
        expect(store.isEnabled('off')).toBe(false)
        expect(store.isEnabled('disabled')).toBe(false)
        expect(store.isEnabled('missing')).toBe(false)
    })

    it('clearConfig 清空配置', () => {
        const store = useConfigStore()
        store.configList = [{ configKey: 'a' }]

        store.clearConfig()

        expect(store.configList).toEqual([])
    })
})

describe('router store', () => {
    beforeEach(() => {
        setActivePinia(createPinia())
    })

    it('initDynamicRoutes 加载动态路由', async () => {
        apiMock.user.routesUser.mockResolvedValueOnce({
            data: { code: 200, data: [{ path: '/admin/user' }] },
        })
        const store = useRouterStore()

        await store.initDynamicRoutes()

        expect(store.dynamicRoutes).toEqual([{ path: '/admin/user' }])
        expect(store.getDynamicRoutes()).toEqual([{ path: '/admin/user' }])
    })

    it('initDynamicRoutes 空数据不覆盖原值', async () => {
        const store = useRouterStore()
        store.setDynamicRoutes([{ path: '/old' }])
        apiMock.user.routesUser.mockResolvedValueOnce({ data: { code: 200, data: [] } })

        await store.initDynamicRoutes()

        expect(store.dynamicRoutes).toEqual([{ path: '/old' }])
    })

    it('initPermissions 加载权限并判断包含关系', async () => {
        apiMock.user.permissionsUser.mockResolvedValueOnce({
            data: { code: 200, data: ['user:query', 'user:add'] },
        })
        const store = useRouterStore()

        await store.initPermissions()

        expect(store.getPermissions()).toEqual(['user:query', 'user:add'])
        expect(store.hasPermission('user:query')).toBe(true)
        expect(store.hasPermission('user:delete')).toBe(false)
    })

    it('initPermissions 空数据保留原权限', async () => {
        const store = useRouterStore()
        store.setPermissions(['old'])
        apiMock.user.permissionsUser.mockResolvedValueOnce({ data: { code: 200, data: [] } })

        await store.initPermissions()

        expect(store.permissions).toEqual(['old'])
    })

    it('setPermissions 可直接注入权限集合', () => {
        const store = useRouterStore()

        store.setPermissions({ includes: () => true })

        expect(store.hasPermission('anything')).toBe(true)
    })
})
