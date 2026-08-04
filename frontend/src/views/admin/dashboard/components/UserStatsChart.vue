<template>
    <div
        class="chart-card rounded-xl bg-white dark:bg-[#2a2b2c] border border-gray-100 dark:border-white/10 p-5"
    >
        <h3 class="text-base font-semibold text-gray-700 dark:text-gray-200 mb-4">用户统计</h3>
        <div ref="chartRef" class="w-full" style="height: 280px"></div>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
    data: { type: Array, default: () => [] },
})

const chartRef = ref()
let chart = null
let resizeObserver = null

const handleResize = () => chart?.resize()

const getOption = () => {
    const months = props.data.map((item) => item.month)
    const newUsers = props.data.map((item) => item.newUsers ?? 0)
    const activeUsers = props.data.map((item) => item.activeUsers ?? 0)

    return {
        tooltip: { trigger: 'axis' },
        legend: { data: ['新增用户', '活跃用户'], top: 0, right: 0 },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
            type: 'category',
            data: months.length ? months : ['1月', '2月', '3月', '4月', '5月', '6月'],
            axisLine: { lineStyle: { color: '#e5e7eb' } },
            axisLabel: { color: '#9ca3af' },
        },
        yAxis: {
            type: 'value',
            axisLine: { show: false },
            splitLine: { lineStyle: { color: '#f3f4f6' } },
            axisLabel: { color: '#9ca3af' },
        },
        series: [
            {
                name: '新增用户',
                type: 'bar',
                barWidth: '30%',
                itemStyle: {
                    borderRadius: [4, 4, 0, 0],
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: '#409EFF' },
                        { offset: 1, color: '#79bbff' },
                    ]),
                },
                data: newUsers.length ? newUsers : [120, 200, 150, 80, 70, 110],
            },
            {
                name: '活跃用户',
                type: 'line',
                smooth: true,
                symbol: 'circle',
                symbolSize: 8,
                lineStyle: { width: 3, color: '#409EFF' },
                itemStyle: { color: '#409EFF' },
                areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: 'rgba(64,158,255,0.25)' },
                        { offset: 1, color: 'rgba(64,158,255,0.02)' },
                    ]),
                },
                data: activeUsers.length ? activeUsers : [220, 280, 230, 260, 270, 300],
            },
        ],
    }
}

function renderChart() {
    if (!chart) return
    chart.setOption(getOption(), true)
}

onMounted(async () => {
    await nextTick()
    if (!chartRef.value) return
    chart = echarts.init(chartRef.value)
    renderChart()
    resizeObserver = new ResizeObserver(handleResize)
    resizeObserver.observe(chartRef.value)
})

watch(() => props.data, renderChart, { deep: true })

onUnmounted(() => {
    resizeObserver?.disconnect()
    chart?.dispose()
})
</script>
