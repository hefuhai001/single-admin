import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { flushPromises } from '@vue/test-utils'

const { routerPush } = vi.hoisted(() => ({ routerPush: vi.fn(() => Promise.resolve()) }))

vi.mock('@/router/index.js', () => ({ default: { push: routerPush } }))

import LoginForm from '@/views/login/components/LoginForm.vue'
import LoginView from '@/views/login/index.vue'
import RegisterView from '@/views/register/index.vue'
import SliderCaptcha from '@/views/register/components/SliderCaptcha.vue'
import { useConfigStore } from '@/stores/modules/config.js'
import { apiMock } from '../helpers/apiMock.js'
import { clickButton, createTestRouter, findButton, mountView } from '../helpers/mountView.js'

const makePinia = (configs = {}) => {
    const pinia = createPinia()
    setActivePinia(pinia)
    const configStore = useConfigStore()
    configStore.configList = Object.entries(configs).map(([configKey, configValue]) => ({
        configKey,
        configValue,
        status: true,
    }))
    return pinia
}

const mountRegister = async (configs) => {
    const pinia = makePinia(configs)
    const router = createTestRouter()
    await router.push('/register')
    const pushSpy = vi.spyOn(router, 'push')
    const wrapper = mountView(RegisterView, { pinia, router })
    await flushPromises()
    return { wrapper, pushSpy }
}

describe('LoginForm', () => {
    it('默认账号通过校验后触发 submit', async () => {
        const pinia = makePinia({ 'register.enable': 'true' })
        const wrapper = mountView(LoginForm, { pinia })
        await flushPromises()

        await clickButton(wrapper, '登 录')
        await flushPromises()

        expect(wrapper.emitted('submit')).toBeTruthy()
        expect(wrapper.emitted('submit')[0][0]).toEqual({ username: 'admin', password: '123456' })
    })

    it('修改输入后提交携带最新表单值', async () => {
        const pinia = makePinia({ 'register.enable': 'true' })
        const wrapper = mountView(LoginForm, { pinia })
        await flushPromises()

        await wrapper.findAll('input')[0].setValue('demo-user')
        await wrapper.findAll('input')[1].setValue('abcdef')
        await clickButton(wrapper, '登 录')
        await flushPromises()

        expect(wrapper.emitted('submit')[0][0]).toEqual({
            username: 'demo-user',
            password: 'abcdef',
        })
    })

    it('注册关闭时不显示注册入口', () => {
        const pinia = makePinia({ 'register.enable': 'false' })
        const wrapper = mountView(LoginForm, { pinia })

        expect(findButton(wrapper, '立即注册')).toBeUndefined()
    })

    it('点击立即注册触发 go-register', async () => {
        const pinia = makePinia({ 'register.enable': 'true' })
        const wrapper = mountView(LoginForm, { pinia })

        await clickButton(wrapper, '立即注册')

        expect(wrapper.emitted('go-register')).toBeTruthy()
    })
})

describe('LoginView', () => {
    beforeEach(() => {
        routerPush.mockClear()
        localStorage.clear()
    })

    it('登录成功后跳转后台并展示 ICP', async () => {
        const pinia = makePinia({ 'register.enable': 'true', 'site.icp': '粤ICP备123号' })
        const wrapper = mountView(LoginView, { pinia })
        await flushPromises()

        await wrapper.findComponent(LoginForm).vm.$emit('submit', {
            username: 'admin',
            password: '123456',
        })
        await flushPromises()

        expect(routerPush).toHaveBeenCalledWith('/admin')
        expect(wrapper.text()).toContain('粤ICP备123号')
    })

    it('登录异常时捕获错误且结束 loading', async () => {
        const pinia = makePinia()
        apiMock.auth.login.mockRejectedValueOnce(new Error('login-error'))
        const errorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
        const wrapper = mountView(LoginView, { pinia })
        await flushPromises()

        await wrapper.findComponent(LoginForm).vm.$emit('submit', { username: 'a', password: 'b' })
        await flushPromises()

        expect(errorSpy).toHaveBeenCalled()
        expect(wrapper.findComponent(LoginForm).props('loading')).toBe(false)
        errorSpy.mockRestore()
    })

    it('点击注册跳转注册页', async () => {
        const pinia = makePinia({ 'register.enable': 'true' })
        const wrapper = mountView(LoginView, { pinia })
        await flushPromises()

        await wrapper.findComponent(LoginForm).vm.$emit('go-register')

        expect(routerPush).toHaveBeenCalledWith('/register')
    })
})

describe('RegisterView', () => {
    beforeEach(() => {
        routerPush.mockClear()
        apiMock.captcha.sendEmailCode.mockClear()
    })

    it('注册开关关闭时显示提示且提交被拦截', async () => {
        const { wrapper, pushSpy } = await mountRegister({ 'register.enable': 'false' })

        expect(wrapper.text()).toContain('注册功能已关闭')

        await clickButton(wrapper, '注 册')
        await flushPromises()

        expect(pushSpy).not.toHaveBeenCalledWith('/admin')
    })

    it('未填邮箱点击获取验证码时提示', async () => {
        const { wrapper } = await mountRegister({
            'register.enable': 'true',
            'register.verify_code.enable': 'true',
        })

        await clickButton(wrapper, '获取验证码')

        expect(wrapper.text()).toContain('获取验证码')
    })

    it('填写邮箱后打开滑块验证并在成功后发送验证码', async () => {
        const { wrapper } = await mountRegister({
            'register.enable': 'true',
            'register.verify_code.enable': 'true',
        })

        await wrapper.findAll('input')[2].setValue('a@b.com')
        await clickButton(wrapper, '获取验证码')
        await flushPromises()

        const captcha = wrapper.findComponent(SliderCaptcha)
        captcha.vm.$emit('success', 'captcha-token')
        await flushPromises()

        expect(apiMock.captcha.sendEmailCode).toHaveBeenCalledWith({
            email: 'a@b.com',
            captchaToken: 'captcha-token',
        })
    })

    it('注册成功后拉取用户信息并跳转后台', async () => {
        const { wrapper, pushSpy } = await mountRegister({ 'register.enable': 'true' })

        const inputs = wrapper.findAll('input')
        await inputs[0].setValue('new-user')
        await inputs[1].setValue('新用户')
        await inputs[2].setValue('new@example.com')
        await inputs[3].setValue('123456')
        await inputs[4].setValue('123456')
        await inputs[5].setValue('13800000000')

        await clickButton(wrapper, '注 册')
        await flushPromises()

        expect(apiMock.auth.register).toHaveBeenCalled()
        expect(pushSpy).toHaveBeenCalledWith('/admin')
    })

    it('点击去登录跳转登录页', async () => {
        const { wrapper, pushSpy } = await mountRegister({ 'register.enable': 'true' })

        await clickButton(wrapper, '去登录')

        expect(pushSpy).toHaveBeenCalledWith('/')
    })
})

describe('SliderCaptcha', () => {
    it('打开时加载验证码并支持换一张', async () => {
        apiMock.captcha.getSliderCaptcha.mockClear()
        const wrapper = mountView(SliderCaptcha, { props: { modelValue: false } })
        await wrapper.setProps({ modelValue: true })
        await flushPromises()

        expect(apiMock.captcha.getSliderCaptcha).toHaveBeenCalledTimes(1)
        expect(wrapper.find('img[src="bg.png"]').exists()).toBe(true)

        await clickButton(wrapper, '换一张')
        await flushPromises()
        expect(apiMock.captcha.getSliderCaptcha).toHaveBeenCalledTimes(2)
    })

    it('滑动距离足够时校验成功并关闭弹窗', async () => {
        apiMock.captcha.verifySlider.mockClear()
        const wrapper = mountView(SliderCaptcha, { props: { modelValue: false } })
        await wrapper.setProps({ modelValue: true })
        await flushPromises()

        const handle = wrapper.find('.cursor-grab')
        await handle.trigger('mousedown', { clientX: 0 })
        document.dispatchEvent(new MouseEvent('mousemove', { clientX: 120 }))
        document.dispatchEvent(new MouseEvent('mouseup'))
        await flushPromises()

        expect(apiMock.captcha.verifySlider).toHaveBeenCalledWith({
            token: 'captcha-token',
            position: 120,
        })
        expect(wrapper.emitted('success')).toBeTruthy()
        expect(wrapper.emitted('update:modelValue').at(-1)).toEqual([false])
        wrapper.unmount()
    })

    it('滑动距离不足时复位提示', async () => {
        const wrapper = mountView(SliderCaptcha, { props: { modelValue: false } })
        await wrapper.setProps({ modelValue: true })
        await flushPromises()

        const handle = wrapper.find('.cursor-grab')
        await handle.trigger('mousedown', { clientX: 0 })
        document.dispatchEvent(new MouseEvent('mouseup'))
        await flushPromises()

        expect(wrapper.text()).toContain('向右滑动滑块填充拼图')
        wrapper.unmount()
    })

    it('校验失败时展示服务端消息并延时刷新', async () => {
        vi.useFakeTimers()
        apiMock.captcha.getSliderCaptcha.mockClear()
        apiMock.captcha.verifySlider.mockRejectedValueOnce({
            response: { data: { msg: '验证码已过期' } },
        })
        const wrapper = mountView(SliderCaptcha, { props: { modelValue: false } })
        await wrapper.setProps({ modelValue: true })
        await flushPromises()

        const handle = wrapper.find('.cursor-grab')
        await handle.trigger('mousedown', { clientX: 0 })
        document.dispatchEvent(new MouseEvent('mousemove', { clientX: 100 }))
        document.dispatchEvent(new MouseEvent('mouseup'))
        await flushPromises()

        expect(wrapper.text()).toContain('验证码已过期')

        vi.advanceTimersByTime(1000)
        await flushPromises()
        expect(apiMock.captcha.getSliderCaptcha).toHaveBeenCalled()
        wrapper.unmount()
        vi.useRealTimers()
    })

    it('验证码加载失败时展示重试提示', async () => {
        apiMock.captcha.getSliderCaptcha.mockRejectedValueOnce(new Error('network'))
        const wrapper = mountView(SliderCaptcha, { props: { modelValue: false } })
        await wrapper.setProps({ modelValue: true })
        await flushPromises()

        expect(wrapper.text()).toContain('加载失败，请重试')
        wrapper.unmount()
    })
})
