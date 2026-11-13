import { describe, expect, it, vi } from 'vitest'
import { flushPromises } from '@vue/test-utils'

import SvgIcon from '@/components/SvgIcon.vue'
import App from '@/App.vue'
import Error403 from '@/components/error/403.vue'
import Error404 from '@/components/error/404.vue'
import GeoBackground from '@/views/login/components/GeoBackground.vue'
import BrandSide from '@/views/login/components/BrandSide.vue'
import StatCard from '@/views/admin/dashboard/components/StatCard.vue'
import UserStatsChart from '@/views/admin/dashboard/components/UserStatsChart.vue'
import PatientStatsChart from '@/views/admin/dashboard/components/PatientStatsChart.vue'
import FileStatsChart from '@/views/admin/dashboard/components/FileStatsChart.vue'
import QuestionnaireStatsChart from '@/views/admin/dashboard/components/QuestionnaireStatsChart.vue'
import DashboardIndex from '@/views/admin/dashboard/index.vue'
import ThemeTest from '@/views/admin/dashboard/ThemeTest.vue'
import FooterCom from '@/views/admin/layout/FooterCom.vue'

import { createTestRouter, findButton, mountView } from '../helpers/mountView.js'
import { chart, echartsMock, resetEchartsMock } from '../helpers/echartsMock.js'

describe('基础组件', () => {
    it('App 渲染 RouterView', async () => {
        const router = createTestRouter()
        await router.push('/')
        const wrapper = mountView(App, { router })

        expect(wrapper.find('.route-view').exists()).toBe(true)
    })

    it('SvgIcon 生成 symbol 与尺寸样式', () => {
        const wrapper = mountView(SvgIcon, {
            props: { name: 'user', prefix: 'custom', color: 'red', size: 20, spin: true },
        })

        const svg = wrapper.find('svg')
        expect(svg.attributes('class')).toContain('svg-icon-spin')
        expect(svg.find('use').attributes('href')).toBe('#custom-user')
        expect(svg.find('use').attributes('fill')).toBe('red')
        expect(svg.attributes('style')).toContain('width: 20px')
        expect(svg.attributes('style')).toContain('height: 20px')
    })

    it('SvgIcon 支持字符串尺寸', () => {
        const wrapper = mountView(SvgIcon, { props: { name: 'logo', size: '2em' } })

        expect(wrapper.find('svg').attributes('style')).toContain('width: 2em')
    })

    it('GeoBackground 渲染 7 个几何装饰', () => {
        const wrapper = mountView(GeoBackground)

        expect(wrapper.findAll('.geo')).toHaveLength(7)
    })

    it('BrandSide 渲染品牌介绍', () => {
        const wrapper = mountView(BrandSide)

        expect(wrapper.text().length).toBeGreaterThan(0)
    })
})

describe('错误页', () => {
    it('404 页面支持返回首页与返回上页', async () => {
        const router = createTestRouter()
        await router.push('/404')
        const pushSpy = vi.spyOn(router, 'push')
        const backSpy = vi.spyOn(router, 'back').mockImplementation(() => {})

        const wrapper = mountView(Error404, { router })
        expect(wrapper.text()).toContain('页面走丢了')

        await findButton(wrapper, '返回首页').trigger('click')
        expect(pushSpy).toHaveBeenCalledWith('/')

        await findButton(wrapper, '返回上页').trigger('click')
        expect(backSpy).toHaveBeenCalled()
    })

    it('403 页面支持返回首页与返回上页', async () => {
        const router = createTestRouter()
        await router.push('/403')
        const pushSpy = vi.spyOn(router, 'push')
        const backSpy = vi.spyOn(router, 'back').mockImplementation(() => {})

        const wrapper = mountView(Error403, { router })
        expect(wrapper.text()).toContain('访问受限')

        await findButton(wrapper, '返回首页').trigger('click')
        expect(pushSpy).toHaveBeenCalledWith('/')

        await findButton(wrapper, '返回上页').trigger('click')
        expect(backSpy).toHaveBeenCalled()
    })
})

describe('仪表盘', () => {
    it('StatCard 展示标题、数值与趋势方向', () => {
        const wrapper = mountView(StatCard, {
            props: { title: '用户总数', value: 100, trend: 12, color: '#000', colorEnd: '#fff' },
        })

        expect(wrapper.text()).toContain('用户总数')
        expect(wrapper.text()).toContain('100')
        expect(wrapper.text()).toContain('+12%')
        expect(wrapper.html()).toContain('linear-gradient')
    })

    it('StatCard 负增长显示减号', () => {
        const wrapper = mountView(StatCard, { props: { title: '下降', value: 1, trend: -3 } })

        expect(wrapper.text()).toContain('-3%')
    })

    it('Dashboard 渲染统计卡片与四个图表', () => {
        const wrapper = mountView(DashboardIndex)

        expect(wrapper.text()).toContain('用户总数')
        expect(wrapper.findAll('.chart-card')).toHaveLength(4)
    })

    it('ThemeTest 渲染三组色板', () => {
        const wrapper = mountView(ThemeTest)

        expect(wrapper.text()).toContain('主色调')
        expect(wrapper.text()).toContain('辅助色')
        expect(wrapper.text()).toContain('中性色')
        expect(wrapper.findAll('[class*="bg-theme-"]')).toHaveLength(11)
    })

    it('FooterCom 读取站点配置', () => {
        const wrapper = mountView(FooterCom)

        expect(wrapper.text()).toContain('Copyright')
        expect(wrapper.text()).toContain('Single Admin')
    })
})

describe('ECharts 图表组件', () => {
    it('UserStatsChart 初始化并在数据变化时重绘', async () => {
        resetEchartsMock()
        const wrapper = mountView(UserStatsChart, { props: { data: [] } })
        await flushPromises()

        expect(echartsMock.init).toHaveBeenCalledTimes(1)
        expect(chart.setOption).toHaveBeenCalledTimes(1)

        await wrapper.setProps({ data: [{ month: '1月', newUsers: 1, activeUsers: 2 }] })
        expect(chart.setOption.mock.calls.length).toBeGreaterThan(1)

        wrapper.unmount()
        expect(chart.dispose).toHaveBeenCalled()
    })

    it('PatientStatsChart 支持有数据与无色值', async () => {
        resetEchartsMock()
        const wrapper = mountView(PatientStatsChart, {
            props: { data: [{ name: 'Male', value: 1 }, { name: 'Other', value: 2 }] },
        })
        await flushPromises()

        const option = chart.setOption.mock.calls[0][0]
        expect(option.series[0].data[1].itemStyle.color).toBe('#9ca3af')
        wrapper.unmount()
    })

    it('FileStatsChart 使用循环色板', async () => {
        resetEchartsMock()
        const wrapper = mountView(FileStatsChart, {
            props: { data: Array.from({ length: 7 }, (_, i) => ({ name: `n${i}`, value: i })) },
        })
        await flushPromises()

        const option = chart.setOption.mock.calls[0][0]
        expect(option.series[0].data).toHaveLength(7)
        wrapper.unmount()
    })

    it('QuestionnaireStatsChart 空数据时 clear，有数据时 setOption', async () => {
        resetEchartsMock()
        const wrapper = mountView(QuestionnaireStatsChart, { props: { data: [] } })
        await flushPromises()

        expect(chart.clear).toHaveBeenCalled()

        await wrapper.setProps({
            data: [{ name: '问卷A', completedCount: 1, pendingCount: 2 }],
        })
        expect(chart.setOption).toHaveBeenCalled()
        wrapper.unmount()
    })
})
