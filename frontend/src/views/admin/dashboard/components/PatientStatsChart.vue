<template>
    <div
        class="chart-card rounded-xl bg-white dark:bg-[#2a2b2c] border border-gray-100 dark:border-white/10 p-5"
    >
        <h3 class="text-base font-semibold text-gray-700 dark:text-gray-200 mb-4">患者统计</h3>
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
    const colorMap = { Male: '#3b82f6', Female: '#ec4899', Unknown: '#9ca3af' }
    const pieData = props.data.length
        ? props.data.map((item) => ({
              value: item.value,
              name: item.name,
              itemStyle: { color: colorMap[item.name] || '#9ca3af' },
          }))
        : []

    return {
        tooltip: { trigger: 'item' },
        legend: { orient: 'vertical', right: '5%', top: 'center' },
        series: [
            {
                type: 'pie',
                radius: ['45%', '70%'],
                center: ['35%', '50%'],
                avoidLabelOverlap: false,
                itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
                label: { show: false },
                emphasis: {
                    label: { show: true, fontSize: 16, fontWeight: 'bold' },
                },
                data: pieData,
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
