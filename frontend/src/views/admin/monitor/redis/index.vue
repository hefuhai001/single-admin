<template>
    <div class="h-full flex flex-col gap-4 overflow-auto">
        <!-- 工具条 -->
        <el-card shadow="never" class="shrink-0">
            <div class="flex items-center gap-4 flex-wrap">
                <span class="text-sm text-gray-500 dark:text-gray-400">点击左侧 Key 树选择 Key，右侧查看详情</span>
                <div class="flex-1"></div>
                <el-button @click="handleRefresh">
                    <el-icon><Refresh /></el-icon> 刷新
                </el-button>
            </div>
        </el-card>

        <!-- 指标卡 -->
        <el-card shadow="never" class="shrink-0">
            <div class="grid grid-cols-2 md:grid-cols-3 xl:grid-cols-6 gap-4">
                <!-- 内存使用率 -->
                <div class="flex items-center gap-3">
                    <el-progress
                        type="dashboard"
                        :width="80"
                        :percentage="memPercent"
                        :color="memColor"
                    >
                        <span class="text-sm font-bold text-gray-700 dark:text-gray-200">{{ memPercent }}%</span>
                    </el-progress>
                    <div>
                        <p class="text-xs text-gray-400">内存使用率</p>
                        <p class="text-sm text-gray-600 dark:text-gray-300">{{ memoryText }}</p>
                    </div>
                </div>
                <div class="flex flex-col justify-center">
                    <p class="text-xs text-gray-400 mb-1">命中率</p>
                    <p class="text-xl font-bold text-gray-800 dark:text-gray-100">{{ hitRate }}%</p>
                    <p class="text-xs text-gray-400 mt-1">Hits {{ info.keyspaceHits ?? 0 }} · Misses {{ info.keyspaceMisses ?? 0 }}</p>
                </div>
                <div class="flex flex-col justify-center">
                    <p class="text-xs text-gray-400 mb-1">Key 总数</p>
                    <p class="text-xl font-bold text-gray-800 dark:text-gray-100">{{ formatNumber(info.totalKeyCount ?? 0) }}</p>
                    <p class="text-xs text-gray-400 mt-1">当前数据库</p>
                </div>
                <div class="flex flex-col justify-center">
                    <p class="text-xs text-gray-400 mb-1">连接数</p>
                    <p class="text-xl font-bold text-gray-800 dark:text-gray-100">{{ info.connectedClients ?? 0 }}</p>
                    <p class="text-xs text-gray-400 mt-1">每秒 {{ formatNumber(info.instantaneousOps ?? 0) }} 次命令</p>
                </div>
                <div class="flex flex-col justify-center">
                    <p class="text-xs text-gray-400 mb-1">运行时间</p>
                    <p class="text-xl font-bold text-gray-800 dark:text-gray-100">{{ uptimeText }}</p>
                    <p class="text-xs text-gray-400 mt-1">{{ info.mode ?? '-' }}</p>
                </div>
                <div class="flex flex-col justify-center">
                    <p class="text-xs text-gray-400 mb-1">Redis 版本</p>
                    <p class="text-xl font-bold text-gray-800 dark:text-gray-100">{{ info.version ?? '-' }}</p>
                    <el-tag size="small" type="primary" class="mt-1 self-start">监控中</el-tag>
                </div>
            </div>
        </el-card>

        <!-- 命令统计 + Key 浏览 -->
        <div class="grid grid-cols-1 xl:grid-cols-3 gap-4 flex-1 min-h-0">
            <!-- 命令统计图表 -->
            <el-card shadow="never" class="xl:col-span-1 min-h-0 flex flex-col">
                <template #header>
                    <span class="font-semibold">命令统计 TOP 10</span>
                </template>
                <div ref="chartRef" class="flex-1 min-h-0" style="min-height: 300px"></div>
            </el-card>

            <!-- Key 浏览：左树右详情 -->
            <el-card shadow="never" class="key-browser-card xl:col-span-2 min-h-0 flex flex-col">
                <div class="flex gap-4 flex-1 min-h-0">
                    <!-- 左侧 Key 树 -->
                    <div class="w-[20vw] h-full shrink-0 border border-gray-100 dark:border-white/10 rounded-lg overflow-auto bg-white dark:bg-[#1d1e1f]">
                        <el-tree
                            ref="treeRef"
                            :data="treeData"
                            node-key="prefix"
                            :props="{ label: 'label', children: 'children' }"
                            highlight-current
                            class="p-2"
                            @node-click="handleNodeClick"
                        >
                            <template #default="{ data }">
                                <span class="flex items-center gap-1">
                                    <el-icon v-if="data.prefix === ''" class="text-[#3b82f6]"><Coin /></el-icon>
                                    <el-icon v-else-if="data.fullKey" class="text-gray-400"><Key /></el-icon>
                                    <el-icon v-else class="text-[#3b82f6]"><Folder /></el-icon>
                                    <span class="truncate">{{ data.label }}</span>
                                    <span class="text-gray-400 text-xs ml-auto">{{ data.count }}</span>
                                </span>
                            </template>
                        </el-tree>
                    </div>
                    <!-- 右侧详情 -->
                    <div class="flex-1 min-w-0 flex flex-col">
                        <div
                            v-if="!detailData.key"
                            class="flex-1 flex flex-col items-center justify-center gap-3 text-gray-400"
                        >
                            <el-icon :size="48"><Key /></el-icon>
                            <span class="text-sm">点击左侧树中的 Key 查看详情</span>
                        </div>
                        <div v-else v-loading="detailLoading" class="flex-1 min-h-0 overflow-hidden">
                            <div class="flex items-center justify-between mb-3">
                                <span class="font-semibold text-sm">Key 详情</span>
                                <el-button
                                    type="danger"
                                    size="small"
                                    @click="handleDeleteOne"
                                    v-if="routerStore.hasPermission('monitor:redis:delete')"
                                >
                                    <el-icon><Delete /></el-icon> 删除此 Key
                                </el-button>
                            </div>
                            <el-descriptions :column="2" border>
                                <el-descriptions-item label="Key">
                                    <span class="font-mono break-all">{{ detailData.key }}</span>
                                </el-descriptions-item>
                                <el-descriptions-item label="类型">
                                    <el-tag :type="typeTag(detailData.type)" size="small">{{ detailData.type }}</el-tag>
                                </el-descriptions-item>
                                <el-descriptions-item label="TTL">
                                    <span v-if="detailData.ttl < 0">永不过期</span>
                                    <span v-else>{{ formatTtl(detailData.ttl) }}</span>
                                </el-descriptions-item>
                                <el-descriptions-item label="长度">{{ detailData.size ?? 0 }}</el-descriptions-item>
                                <el-descriptions-item label="值预览" :span="2">
                                    <pre
                                        v-if="detailData.valuePreview !== null && detailData.valuePreview !== undefined"
                                        class="text-sm bg-gray-50 dark:bg-[#1d1e1f] rounded p-3 overflow-auto max-h-80 whitespace-pre-wrap break-all"
                                    >{{ detailData.valuePreview }}</pre>
                                    <span v-else class="text-gray-400">非 String 类型，无值预览</span>
                                </el-descriptions-item>
                            </el-descriptions>
                        </div>
                    </div>
                </div>
            </el-card>
        </div>
    </div>
</template>

<style scoped>
/* 让卡片 body 变为 flex 容器并填满剩余高度，保证左侧树可滚动、右侧详情固定 */
.key-browser-card :deep(.el-card__body) {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
}
</style>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Delete, Folder, Key, Coin } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import api from '@/api/index.js'
import { useRouterStore } from '@/stores/modules/router.js'

const routerStore = useRouterStore()

const info = ref({})
const commandStats = ref([])

// ==================== Key 树 ====================
const treeRef = ref()
const treeRaw = ref([])
const currentKey = ref('')
const treeData = computed(() => [
    {
        label: '全部 Key',
        prefix: '',
        fullKey: null,
        count: info.value.totalKeyCount ?? 0,
        children: treeRaw.value,
    },
])

async function loadTree() {
    try {
        const res = await api.redis.getRedisKeyTree()
        treeRaw.value = res.data.data || []
        expandAllNodes()
    } catch (error) {
        console.error('获取 Key 树失败', error)
    }
}

/** 展开全部树节点（数据异步到达后 el-tree 的 default-expand-all 不生效，需手动展开） */
function expandAllNodes() {
    nextTick(() => {
        const nodesMap = treeRef.value?.store?.nodesMap
        if (!nodesMap) return
        Object.values(nodesMap).forEach((node) => {
            node.expanded = true
        })
    })
}

function handleNodeClick(data) {
    // 仅真实 Key 节点展示详情，纯命名空间节点仅用于展开
    if (!data.fullKey) return
    currentKey.value = data.fullKey
    handleKeySelect(data.fullKey)
}

// ==================== 详情 ====================
const detailLoading = ref(false)
const detailData = ref({})

async function handleKeySelect(key) {
    detailLoading.value = true
    try {
        const res = await api.redis.getRedisKeyDetail(key)
        detailData.value = res.data.data || {}
    } catch (error) {
        console.error('获取 Key 详情失败', error)
        detailData.value = {}
    } finally {
        detailLoading.value = false
    }
}

async function handleDeleteOne() {
    if (!detailData.value.key) return
    try {
        await ElMessageBox.confirm(
            `确定要删除 Key "${detailData.value.key}" 吗？删除后不可恢复！`,
            '删除确认',
            { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
        )
        const res = await api.redis.deleteRedisKeys([detailData.value.key])
        ElMessage.success(res.data?.msg || '删除成功')
        currentKey.value = ''
        detailData.value = {}
        loadInfo()
        loadTree()
    } catch (error) {
        if (error !== 'cancel') {
            console.error('删除 Key 失败', error)
        }
    }
}

function handleRefresh() {
    loadInfo()
    loadCommandStats()
    loadTree()
    if (currentKey.value) {
        handleKeySelect(currentKey.value)
    }
}

// ==================== 指标 ====================
const memPercent = computed(() => {
    const used = info.value.usedMemoryBytes ?? 0
    const max = info.value.maxmemoryBytes ?? 0
    if (max <= 0) return 0
    return Math.min(100, Math.round((used / max) * 100))
})
const memColor = computed(() => {
    if (memPercent.value >= 80) return '#ef4444'
    if (memPercent.value >= 60) return '#f59e0b'
    return '#3b82f6'
})
const memoryText = computed(() => {
    const max = info.value.maxmemoryBytes ?? 0
    if (max <= 0) return '未限制'
    return formatBytes(info.value.usedMemoryBytes ?? 0) + ' / ' + formatBytes(max)
})
const hitRate = computed(() => {
    const hits = info.value.keyspaceHits ?? 0
    const misses = info.value.keyspaceMisses ?? 0
    const total2 = hits + misses
    if (total2 === 0) return 0
    return ((hits / total2) * 100).toFixed(2)
})
const uptimeText = computed(() => formatUptime(info.value.uptimeSeconds ?? 0))

function formatBytes(bytes) {
    if (!bytes && bytes !== 0) return '0 B'
    const units = ['B', 'KB', 'MB', 'GB', 'TB']
    let i = 0
    let value = bytes
    while (value >= 1024 && i < units.length - 1) {
        value /= 1024
        i++
    }
    return value.toFixed(value >= 100 || i === 0 ? 0 : 1) + ' ' + units[i]
}
function formatNumber(num) {
    if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
    return String(num)
}
function formatTtl(seconds) {
    if (seconds >= 86400) return Math.floor(seconds / 86400) + '天' + Math.floor((seconds % 86400) / 3600) + '时'
    if (seconds >= 3600) return Math.floor(seconds / 3600) + '时' + Math.floor((seconds % 3600) / 60) + '分'
    if (seconds >= 60) return Math.floor(seconds / 60) + '分' + (seconds % 60) + '秒'
    return seconds + '秒'
}
function formatUptime(seconds) {
    const days = Math.floor(seconds / 86400)
    const hours = Math.floor((seconds % 86400) / 3600)
    const mins = Math.floor((seconds % 3600) / 60)
    if (days > 0) return days + '天' + hours + '时'
    if (hours > 0) return hours + '时' + mins + '分'
    return mins + '分'
}
function typeTag(type) {
    const map = { STRING: 'primary', LIST: 'success', SET: 'warning', HASH: 'danger', ZSET: 'info' }
    return map[type] || 'info'
}

// ==================== 数据加载 ====================
async function loadInfo() {
    try {
        const res = await api.redis.getRedisInfo()
        info.value = res.data.data || {}
    } catch (error) {
        console.error('获取 Redis 信息失败', error)
    }
}

async function loadCommandStats() {
    try {
        const res = await api.redis.getCommandStats()
        commandStats.value = res.data.data || []
        renderCommandChart()
    } catch (error) {
        console.error('获取命令统计失败', error)
    }
}

// ==================== 图表 ====================
const chartRef = ref()
let chart = null
let resizeObserver = null

function renderCommandChart() {
    if (!chart) return
    const top10 = commandStats.value.slice(0, 10)
    const names = top10.map((item) => item.command)
    const calls = top10.map((item) => item.calls)
    chart.setOption(
        {
            tooltip: {
                trigger: 'axis',
                axisPointer: { type: 'shadow' },
                formatter(params) {
                    const item = params[0]
                    const stat = top10[item.dataIndex]
                    return `${stat.command}<br/>调用次数：${stat.calls}<br/>累计耗时：${stat.usec}μs<br/>平均耗时：${stat.usecPerCall}μs`
                },
            },
            grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
            xAxis: {
                type: 'value',
                axisLine: { show: false },
                splitLine: { lineStyle: { color: '#f3f4f6' } },
                axisLabel: { color: '#9ca3af' },
            },
            yAxis: {
                type: 'category',
                data: names,
                axisLine: { lineStyle: { color: '#e5e7eb' } },
                axisLabel: { color: '#9ca3af' },
            },
            series: [
                {
                    name: '调用次数',
                    type: 'bar',
                    barWidth: '55%',
                    itemStyle: {
                        borderRadius: [0, 4, 4, 0],
                        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                            { offset: 0, color: '#3b82f6' },
                            { offset: 1, color: '#60a5fa' },
                        ]),
                    },
                    data: calls,
                },
            ],
        },
        true
    )
}

onMounted(async () => {
    await nextTick()
    if (chartRef.value) {
        chart = echarts.init(chartRef.value)
        resizeObserver = new ResizeObserver(() => chart?.resize())
        resizeObserver.observe(chartRef.value)
    }
    loadInfo()
    loadCommandStats()
    loadTree()
})

onUnmounted(() => {
    resizeObserver?.disconnect()
    chart?.dispose()
})
</script>
