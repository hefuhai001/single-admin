import { vi } from 'vitest'

export const chart = {
    setOption: vi.fn(),
    resize: vi.fn(),
    dispose: vi.fn(),
    clear: vi.fn(),
}

export const echartsMock = {
    init: vi.fn(() => chart),
    graphic: {
        LinearGradient: vi.fn(function LinearGradient() {}),
    },
    use: vi.fn(),
}

export function resetEchartsMock() {
    chart.setOption.mockClear()
    chart.resize.mockClear()
    chart.dispose.mockClear()
    chart.clear.mockClear()
    echartsMock.init.mockClear()
}
