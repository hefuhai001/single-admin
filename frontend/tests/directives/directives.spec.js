import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { ElMessage } = vi.hoisted(() => ({
    ElMessage: vi.fn(),
}))

vi.mock('element-plus', () => ({ ElMessage }))

import copy from '@/directives/modules/copy.js'
import debounce from '@/directives/modules/debounce.js'
import throttle from '@/directives/modules/throttle.js'
import directives from '@/directives/index.js'

const flush = () => Promise.resolve()

describe('v-copy', () => {
    let writeText

    beforeEach(() => {
        ElMessage.mockClear()
        writeText = vi.fn(() => Promise.resolve())
        Object.defineProperty(navigator, 'clipboard', {
            configurable: true,
            value: { writeText },
        })
        document.execCommand = vi.fn(() => true)
    })

    afterEach(() => {
        vi.restoreAllMocks()
    })

    it('点击时复制字符串值', async () => {
        const el = document.createElement('button')
        copy.mounted(el, { value: 'hello' })

        el.click()
        await flush()

        expect(writeText).toHaveBeenCalledWith('hello')
        expect(ElMessage).toHaveBeenCalledWith({ type: 'success', message: '复制成功' })

        copy.beforeUnmount(el)
    })

    it('支持函数类型的绑定值并在 updated 时更新', async () => {
        const el = document.createElement('button')
        copy.mounted(el, { value: () => 'first' })
        copy.updated(el, { value: () => 'second' })

        el.click()
        await flush()

        expect(writeText).toHaveBeenCalledWith('second')
    })

    it('Clipboard API 不可用时降级为 execCommand', async () => {
        Object.defineProperty(navigator, 'clipboard', { configurable: true, value: undefined })
        const el = document.createElement('button')
        copy.mounted(el, { value: 'fallback' })

        el.click()
        await flush()

        expect(document.execCommand).toHaveBeenCalledWith('copy')
        expect(ElMessage).toHaveBeenCalledWith({ type: 'success', message: '复制成功' })
    })

    it('降级复制也失败时提示错误', async () => {
        Object.defineProperty(navigator, 'clipboard', { configurable: true, value: undefined })
        document.execCommand = vi.fn(() => {
            throw new Error('exec failed')
        })
        const el = document.createElement('button')
        copy.mounted(el, { value: 'fallback' })

        el.click()
        await flush()

        expect(ElMessage).toHaveBeenCalledWith({ type: 'error', message: '复制失败，请手动复制' })
    })
})

describe('v-debounce', () => {
    beforeEach(() => {
        vi.useFakeTimers()
    })

    afterEach(() => {
        vi.useRealTimers()
    })

    it('函数参数时默认延迟 1000ms 且连续触发只执行最后一次', () => {
        const handler = vi.fn()
        const el = document.createElement('input')
        debounce.mounted(el, { value: handler })

        el.dispatchEvent(new Event('input'))
        el.dispatchEvent(new Event('input'))
        vi.advanceTimersByTime(999)
        expect(handler).not.toHaveBeenCalled()

        vi.advanceTimersByTime(1)
        expect(handler).toHaveBeenCalledTimes(1)

        debounce.beforeUnmount(el, { value: handler })
    })

    it('对象参数可自定义事件与延迟', () => {
        const handler = vi.fn()
        const el = document.createElement('button')
        debounce.mounted(el, { value: { handler, delay: 200 }, arg: 'click' })

        el.click()
        vi.advanceTimersByTime(200)

        expect(handler).toHaveBeenCalledTimes(1)
        debounce.beforeUnmount(el, { value: { handler }, arg: 'click' })
    })

    it('updated 重新绑定新的处理函数', () => {
        const first = vi.fn()
        const second = vi.fn()
        const el = document.createElement('input')
        debounce.mounted(el, { value: first })
        debounce.updated(el, { value: second })

        el.dispatchEvent(new Event('input'))
        vi.advanceTimersByTime(1000)

        expect(first).toHaveBeenCalledTimes(1)
        expect(second).not.toHaveBeenCalled()
    })
})

describe('v-throttle', () => {
    beforeEach(() => {
        vi.useFakeTimers()
        vi.setSystemTime(new Date(50))
    })

    afterEach(() => {
        vi.useRealTimers()
    })

    it('默认 leading 首次立即执行，延迟内不重复执行', () => {
        const handler = vi.fn()
        const el = document.createElement('button')
        throttle.mounted(el, { value: handler })

        el.click()
        el.click()

        expect(handler).toHaveBeenCalledTimes(1)
        throttle.beforeUnmount(el, { arg: 'click' })
    })

    it('超过延迟时间后再次执行', () => {
        const handler = vi.fn()
        const el = document.createElement('button')
        throttle.mounted(el, { value: { handler, delay: 100 } })

        el.click()
        vi.advanceTimersByTime(100)
        el.click()

        expect(handler).toHaveBeenCalledTimes(2)
    })

    it('trailing 模式在延迟结束后补执行一次', () => {
        const handler = vi.fn()
        const el = document.createElement('button')
        throttle.mounted(el, {
            value: { handler, delay: 100, leading: false, trailing: true },
            arg: 'click',
        })

        el.click()
        expect(handler).not.toHaveBeenCalled()

        vi.advanceTimersByTime(100)
        expect(handler).toHaveBeenCalledTimes(1)
    })

    it('leading=false 且 trailing=false 时不执行', () => {
        const handler = vi.fn()
        const el = document.createElement('button')
        throttle.mounted(el, { value: { handler, delay: 100, leading: false } })

        el.click()
        vi.advanceTimersByTime(500)

        expect(handler).not.toHaveBeenCalled()
    })

    it('updated 重绑处理函数', () => {
        const second = vi.fn()
        const el = document.createElement('button')
        throttle.mounted(el, { value: vi.fn() })
        throttle.updated(el, { value: second })

        el.throttleHandler()

        expect(second).toHaveBeenCalledTimes(1)
        throttle.beforeUnmount(el, { arg: 'click' })
    })
})

describe('directives install', () => {
    it('向应用注册 copy/debounce/throttle', () => {
        const app = { directive: vi.fn() }

        directives.install(app)

        expect(app.directive).toHaveBeenCalledWith('copy', copy)
        expect(app.directive).toHaveBeenCalledWith('debounce', debounce)
        expect(app.directive).toHaveBeenCalledWith('throttle', throttle)
    })
})
