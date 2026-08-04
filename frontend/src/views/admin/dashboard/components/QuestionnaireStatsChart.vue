<template>
    <div
        class="chart-card rounded-xl bg-white dark:bg-[#2a2b2c] border border-gray-100 dark:border-white/10 p-5"
    >
        <h3 class="text-base font-semibold text-gray-700 dark:text-gray-200 mb-4">问卷统计</h3>
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
    const names = props.data.map((item) => item.name)
    const completedData = props.data.map((item) => item.completedCount ?? 0)
    const pendingData = props.data.map((item) => item.pendingCount ?? 0)

    return {
        tooltip: { trigger: 'axis' },
        legend: { data: ['已填写', '未填写'], top: 0, right: 0 },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
            type: 'category',
            data: names.length
                ? names
                : ['健康评估', '心理测评', '营养调查', '满意度', '随访记录', '生活质量'],
            axisLine: { lineStyle: { color: '#e5e7eb' } },
            axisLabel: { color: '#9ca3af', rotate: 30 },
        },
        yAxis: {
            type: 'value',
            axisLine: { show: false },
            splitLine: { lineStyle: { color: '#f3f4f6' } },
            axisLabel: { color: '#9ca3af' },
        },
        series: [
            {
                name: '已填写',
                type: 'bar',
                stack: 'total',
                barWidth: '40%',
                itemStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: '#34d399' },
                        { offset: 1, color: '#6ee7b7' },
                    ]),
                },
                data: completedData.length ? completedData : [320, 302, 301, 334, 390, 330],
            },
            {
                name: '未填写',
                type: 'bar',
                stack: 'total',
                itemStyle: {
                    borderRadius: [4, 4, 0, 0],
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: '#fbbf24' },
                        { offset: 1, color: '#fcd34d' },
                    ]),
                },
                data: pendingData.length ? pendingData : [120, 132, 101, 134, 90, 70],
            },
        ],
    }
}

function renderChart() {
    if (!chart) return
    if (!props.data.length) {
        chart.clear()
        return
    }
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
