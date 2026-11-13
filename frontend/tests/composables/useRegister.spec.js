import { beforeEach, describe, expect, it, vi } from 'vitest'

const { configStore, authStore, ElMessage, apiMock } = vi.hoisted(() => {
    const ElMessage = Object.assign(vi.fn(), {
        success: vi.fn(),
        error: vi.fn(),
        warning: vi.fn(),
    })
    return {
        configStore: { isEnabled: vi.fn(() => true) },
        authStore: { register: vi.fn(() => Promise.resolve(true)) },
        ElMessage,
        apiMock: { sendEmailCode: vi.fn(() => Promise.resolve()) },
    }
})

vi.mock('element-plus', () => ({ ElMessage }))
vi.mock('@/stores/modules/config.js', () => ({ useConfigStore: () => configStore }))
vi.mock('@/stores/modules/auth.js', () => ({ useAuthStore: () => authStore }))
vi.mock('@/api/index.js', () => ({ default: { captcha: { sendEmailCode: apiMock.sendEmailCode } } }))

import { useRegisterForm } from '@/composables/useRegister.js'

describe('useRegisterForm', () => {
    beforeEach(() => {
        configStore.isEnabled.mockReset().mockReturnValue(true)
        authStore.register.mockReset().mockResolvedValue(true)
        apiMock.sendEmailCode.mockReset().mockResolvedValue()
        ElMessage.mockClear()
        ElMessage.success.mockClear()
        ElMessage.error.mockClear()
        ElMessage.warning.mockClear()
        vi.useRealTimers()
    })

    it('根据配置计算注册与验证码开关', () => {
        configStore.isEnabled.mockImplementation((key) => key === 'register.enable')
        const form = useRegisterForm()

        expect(form.registerEnabled.value).toBe(true)
        expect(form.verifyCodeEnabled.value).toBe(false)
    })

    it('openCaptcha 未填邮箱时提示并返回 false', () => {
        const form = useRegisterForm()

        expect(form.openCaptcha()).toBe(false)
        expect(ElMessage.warning).toHaveBeenCalledWith('请先填写邮箱')
        expect(form.captchaVisible.value).toBe(false)
    })

    it('openCaptcha 填写邮箱后打开验证弹窗', () => {
        const form = useRegisterForm()
        form.registerForm.email = 'a@b.com'

        expect(form.openCaptcha()).toBe(true)
        expect(form.captchaVisible.value).toBe(true)
    })

    it('滑块验证成功后发送验证码并开始倒计时', async () => {
        vi.useFakeTimers()
        const form = useRegisterForm()
        form.registerForm.email = 'a@b.com'

        await form.handleCaptchaSuccess('captcha-token')

        expect(apiMock.sendEmailCode).toHaveBeenCalledWith({
            email: 'a@b.com',
            captchaToken: 'captcha-token',
        })
        expect(ElMessage.success).toHaveBeenCalledWith('验证码已发送')
        expect(form.codeCountdown.value).toBe(60)
        expect(form.codeButtonText.value).toBe('60秒后重试')

        vi.advanceTimersByTime(60000)
        expect(form.codeCountdown.value).toBe(0)
        expect(form.codeButtonText.value).toBe('获取验证码')
        expect(form.codeLoading.value).toBe(false)
    })

    it('发送验证码失败时展示服务端消息', async () => {
        apiMock.sendEmailCode.mockRejectedValue({ response: { data: { msg: '发送频繁' } } })
        const form = useRegisterForm()

        await form.sendEmailCode('token')

        expect(ElMessage.error).toHaveBeenCalledWith('发送频繁')
        expect(form.codeLoading.value).toBe(false)
    })

    it('发送验证码失败且无服务端消息时使用默认提示', async () => {
        apiMock.sendEmailCode.mockRejectedValue(new Error('network'))
        const form = useRegisterForm()

        await form.sendEmailCode('token')

        expect(ElMessage.error).toHaveBeenCalledWith('验证码发送失败')
    })

    it('注册关闭时直接返回 false', async () => {
        configStore.isEnabled.mockReturnValue(false)
        const form = useRegisterForm()

        expect(await form.submitRegister()).toBe(false)
        expect(ElMessage.warning).toHaveBeenCalledWith('注册功能已关闭')
    })

    it('表单校验失败时提示并返回 false', async () => {
        const form = useRegisterForm()
        form.registerFormRef.value = { validate: vi.fn(() => Promise.reject(new Error('invalid'))) }

        expect(await form.submitRegister()).toBe(false)
        expect(ElMessage.error).toHaveBeenCalledWith('请填写正确的注册信息')
    })

    it('表单校验返回 false 时返回 false', async () => {
        const form = useRegisterForm()
        form.registerFormRef.value = { validate: vi.fn(() => Promise.resolve(false)) }

        expect(await form.submitRegister()).toBe(false)
    })

    it('校验通过后调用注册并返回结果', async () => {
        const form = useRegisterForm()
        form.registerFormRef.value = { validate: vi.fn(() => Promise.resolve(true)) }
        form.registerForm.username = 'new-user'

        expect(await form.submitRegister()).toBe(true)
        expect(authStore.register).toHaveBeenCalledWith(form.registerForm)
        expect(form.loading.value).toBe(false)
    })

    it('注册接口异常时提示并返回 false', async () => {
        authStore.register.mockRejectedValue({ response: { data: { msg: '用户名已存在' } } })
        const form = useRegisterForm()
        form.registerFormRef.value = { validate: vi.fn(() => Promise.resolve(true)) }

        expect(await form.submitRegister()).toBe(false)
        expect(ElMessage.error).toHaveBeenCalledWith('用户名已存在')
    })

    it('注册异常且无服务端消息时使用默认提示', async () => {
        authStore.register.mockRejectedValue(new Error('network'))
        const form = useRegisterForm()
        form.registerFormRef.value = { validate: vi.fn(() => Promise.resolve(true)) }

        await form.submitRegister()

        expect(ElMessage.error).toHaveBeenCalledWith('注册失败')
    })

    it('确认密码校验器校验一致性', () => {
        const form = useRegisterForm()
        form.registerForm.password = '123456'
        const rule = form.registerRules.confirmPassword.find((item) => item.validator)
        const callback = vi.fn()

        rule.validator({}, '654321', callback)
        expect(callback).toHaveBeenCalledWith(expect.any(Error))

        callback.mockClear()
        rule.validator({}, '123456', callback)
        expect(callback).toHaveBeenCalledWith()
    })
})
