import { vi } from 'vitest'

// 统一 mock 接口层，组件与状态测试均不访问真实后端
vi.mock('@/api/index.js', async () => {
    const { apiMock } = await import('./helpers/apiMock.js')
    return { default: apiMock }
})

// 直接引用 api/modules 的视图也走统一桩，避免真实网络请求
vi.mock('@/utils/requestUtil.js', async () => {
    const { requestMock } = await import('./helpers/requestMock.js')
    return { default: requestMock }
})

// ECharts 依赖 canvas，测试环境统一替换为可断言的桩
vi.mock('echarts', async () => {
    const { echartsMock } = await import('./helpers/echartsMock.js')
    return echartsMock
})

// jsdom 缺失的浏览器 API 补丁，供 Element Plus / ECharts 等组件使用
class ResizeObserverMock {
    observe() {}
    unobserve() {}
    disconnect() {}
}

class IntersectionObserverMock {
    observe() {}
    unobserve() {}
    disconnect() {}
    takeRecords() {
        return []
    }
}

globalThis.ResizeObserver = globalThis.ResizeObserver || ResizeObserverMock
globalThis.IntersectionObserver = globalThis.IntersectionObserver || IntersectionObserverMock

if (!window.matchMedia) {
    window.matchMedia = (query) => ({
        matches: false,
        media: query,
        onchange: null,
        addListener: vi.fn(),
        removeListener: vi.fn(),
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
        dispatchEvent: vi.fn(),
    })
}

window.scrollTo = window.scrollTo || vi.fn()
Element.prototype.scrollIntoView = Element.prototype.scrollIntoView || vi.fn()

if (!globalThis.crypto?.randomUUID) {
    globalThis.crypto = {
        ...globalThis.crypto,
        randomUUID: () => '00000000-0000-4000-8000-000000000000',
    }
}
