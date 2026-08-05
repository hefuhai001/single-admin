<template>
    <div
        class="flex items-center bg-white/90 backdrop-blur-sm border-b border-gray-200 dark:bg-[#1d1e1f]/90 dark:border-gray-700 overflow-hidden w-full"
    >
        <el-tabs
            v-model="tabsStore.activeTab"
            type="card"
            class="flex-1 ml-2 min-w-0 custom-tabs"
            @tab-click="handleTabClick"
            @tab-remove="handleTabRemove"
            @contextmenu.prevent="onTabsContextMenu"
        >
            <el-tab-pane
                v-for="tab in tabsStore.visitedTabs"
                :key="tab.fullPath"
                :label="tab.title"
                :name="tab.fullPath"
                :closable="!tab.affix"
            />
        </el-tabs>
    </div>
</template>

<script setup>
import { h, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTabsStore } from '@/stores/modules/tabs'
import ContextMenu from '@imengyu/vue3-context-menu'
import { Refresh, Close, Back, Right, CircleClose } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const tabsStore = useTabsStore()

const handleTabClick = (tab) => {
    tabsStore.setActiveTab(tab.props.name)
}

const handleTabRemove = (fullPath) => {
    tabsStore.closeTab(fullPath)
}

const handleCommand = (command) => {
    switch (command) {
        case 'refresh':
            router.replace({ path: '' + route.path })
            break
        case 'closeOther':
            tabsStore.closeOtherTabs(tabsStore.activeTab)
            break
        case 'closeLeft':
            tabsStore.closeLeftTabs(tabsStore.activeTab)
            break
        case 'closeRight':
            tabsStore.closeRightTabs(tabsStore.activeTab)
            break
        case 'closeAll':
            tabsStore.closeAllTabs()
            break
    }
}

const onTabsContextMenu = (e) => {
    ContextMenu.showContextMenu({
        x: e.x,
        y: e.y,
        items: [
            {
                label: '刷新当前页',
                icon: h(Refresh, { style: { width: '16px', height: '16px' } }),
                onClick: () => handleCommand('refresh'),
            },
            {
                label: '关闭其他',
                icon: h(Close, { style: { width: '16px', height: '16px' } }),
                divided: true,
                onClick: () => handleCommand('closeOther'),
            },
            {
                label: '关闭左侧',
                icon: h(Back, { style: { width: '16px', height: '16px' } }),
                onClick: () => handleCommand('closeLeft'),
            },
            {
                label: '关闭右侧',
                icon: h(Right, { style: { width: '16px', height: '16px' } }),
                onClick: () => handleCommand('closeRight'),
            },
            {
                label: '关闭所有',
                icon: h(CircleClose, {
                    style: { width: '16px', height: '16px' },
                }),
                divided: true,
                onClick: () => handleCommand('closeAll'),
            },
        ],
    })
}

watch(
    () => route.fullPath,
    () => {
        tabsStore.addTab(route)
    },
    { immediate: true },
)

onMounted(() => {
    tabsStore.addTab(route)
})
</script>

<style scoped>
/* Tabs 样式优化 */
.custom-tabs :deep(.el-tabs__header) {
    margin: 0;
    border-bottom: none;
    overflow: hidden;
}

.custom-tabs :deep(.el-tabs__nav-wrap) {
    overflow: hidden;
    height: 32px;
    line-height: 32px;
}

.custom-tabs :deep(.el-tabs__nav-scroll) {
    overflow: hidden;
}

.custom-tabs :deep(.el-tabs__nav) {
    border: none;
    gap: 4px;
    display: flex;
    align-items: center;
}

.custom-tabs :deep(.el-tabs__nav-prev),
.custom-tabs :deep(.el-tabs__nav-next) {
    height: 32px;
    line-height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.custom-tabs :deep(.el-tabs__item) {
    height: 32px;
    line-height: 32px;
    border: 1px solid #e5e7eb;
    border-radius: 6px;
    margin: 0 2px;
    padding: 0 16px;
    font-size: 13px;
    font-weight: 500;
    color: #6b7280;
    transition: all 0.2s ease;
    background-color: transparent;
}

.dark .custom-tabs :deep(.el-tabs__item) {
    color: #9ca3af;
    border-color: #374151;
}

.custom-tabs :deep(.el-tabs__item:hover) {
    color: var(--el-color-primary);
    background-color: var(--el-color-primary-light-9);
    border-color: var(--el-color-primary);
}

.dark .custom-tabs :deep(.el-tabs__item:hover) {
    color: var(--el-color-primary-light-3);
    background-color: #374151;
    border-color: var(--el-color-primary-light-3);
}

/* 激活状态样式 */
.custom-tabs :deep(.el-tabs__item.is-active) {
    color: var(--el-color-primary);
    background-color: var(--el-color-primary-light-9);
    border-color: var(--el-color-primary);
}

.dark .custom-tabs :deep(.el-tabs__item.is-active) {
    color: #ffffff;
    background-color: var(--el-color-primary-dark-2);
    border-color: var(--el-color-primary);
}

/* 关闭图标样式 */
.custom-tabs :deep(.el-tabs__item .el-icon-close) {
    width: 14px;
    height: 14px;
    font-size: 12px;
    margin-left: 8px;
    border-radius: 3px;
    transition: all 0.2s ease;
}

.custom-tabs :deep(.el-tabs__item .el-icon-close:hover) {
    background-color: rgba(0, 0, 0, 0.1);
    color: #ef4444;
}

.dark .custom-tabs :deep(.el-tabs__item .el-icon-close:hover) {
    background-color: rgba(255, 255, 255, 0.1);
}

/* 第一个标签页（固定标签）特殊处理 */
.custom-tabs :deep(.el-tabs__item:first-child) {
    margin-left: 0;
}
</style>
