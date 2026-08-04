<template>
    <el-drawer
        v-model="drawerVisible"
        title="主题配置"
        direction="rtl"
        size="420px"
        :destroy-on-close="false"
        class="theme-drawer"
    >
        <div class="theme-panel">
            <section class="mb-8">
                <h3
                    class="text-sm font-bold text-gray-700 dark:text-gray-300 mb-4 uppercase tracking-widest flex items-center gap-2"
                >
                    <span
                        class="w-1 h-4 bg-gradient-to-b from-blue-500 to-purple-600 rounded-full"
                    ></span>
                    外观模式
                </h3>
                <div class="flex gap-3">
                    <el-button
                        :type="!themeStore.isDark ? 'primary' : 'default'"
                        class="flex-1 transition-all duration-300 hover:scale-105 shadow-md hover:shadow-lg"
                        @click="toggleTheme(false)"
                    >
                        <el-icon class="mr-2"><Sunny /></el-icon>
                        浅色
                    </el-button>
                    <el-button
                        :type="themeStore.isDark ? 'primary' : 'default'"
                        class="flex-1 transition-all duration-300 hover:scale-105 shadow-md hover:shadow-lg"
                        @click="toggleTheme(true)"
                    >
                        <el-icon class="mr-2"><Moon /></el-icon>
                        深色
                    </el-button>
                </div>
            </section>

            <section class="mb-8">
                <h3
                    class="text-sm font-bold text-gray-700 dark:text-gray-300 mb-4 uppercase tracking-widest flex items-center gap-2"
                >
                    <span
                        class="w-1 h-4 bg-gradient-to-b from-blue-500 to-purple-600 rounded-full"
                    ></span>
                    主题色
                </h3>
                <div class="grid grid-cols-2 gap-4">
                    <div
                        v-for="theme in themeStore.getThemePresets()"
                        :key="theme.key"
                        @click="selectTheme(theme.key)"
                        class="relative p-4 rounded-2xl cursor-pointer transition-all duration-300 transform hover:scale-105 hover:shadow-xl"
                        :class="[
                            themeStore.getThemeKey === theme.key
                                ? 'ring-2 ring-blue-500 bg-gradient-to-br from-blue-50 to-purple-50 dark:from-gray-800 dark:to-gray-900 shadow-lg'
                                : 'border-2 border-gray-200 dark:border-gray-700 hover:border-blue-300 hover:shadow-md bg-white dark:bg-gray-800',
                        ]"
                    >
                        <div class="flex items-center gap-3">
                            <div
                                class="w-12 h-12 rounded-xl shadow-lg flex items-center justify-center text-xl transform transition-transform duration-300 hover:rotate-12"
                                :style="{ background: theme.preview }"
                            >
                                {{ theme.icon }}
                            </div>
                            <div>
                                <div class="font-bold text-sm text-gray-800 dark:text-gray-200">
                                    {{ theme.label }}
                                </div>
                                <div class="text-xs text-gray-500 dark:text-gray-400">
                                    {{ theme.description }}
                                </div>
                            </div>
                        </div>

                        <div
                            v-if="themeStore.getThemeKey === theme.key"
                            class="absolute top-2 right-2 w-6 h-6 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 text-white flex items-center justify-center text-xs shadow-lg animate-pulse"
                        >
                            <el-icon><Check /></el-icon>
                        </div>
                    </div>
                </div>
            </section>

            <section class="mt-8 pt-6 border-t border-gray-200 dark:border-gray-700">
                <div
                    class="bg-gradient-to-br from-blue-50 to-purple-50 dark:from-gray-800 dark:to-gray-900 rounded-2xl p-6 text-center"
                >
                    <div class="text-sm text-gray-600 dark:text-gray-400 mb-3">
                        当前主题：<span class="font-bold text-blue-600 dark:text-blue-400">{{
                            themeStore.currentTheme?.label
                        }}</span>
                    </div>
                    <div class="flex items-center justify-center gap-2">
                        <div
                            class="w-8 h-8 rounded-full shadow-lg"
                            :style="{ background: themeStore.getPrimaryColor }"
                        ></div>
                        <div class="text-xs text-gray-500 dark:text-gray-400 font-mono">
                            {{ themeStore.getPrimaryColor }}
                        </div>
                    </div>
                </div>
            </section>

            <section class="mt-8 pt-6 border-t border-gray-200 dark:border-gray-700">
                <h3
                    class="text-sm font-bold text-gray-700 dark:text-gray-300 mb-4 uppercase tracking-widest flex items-center gap-2"
                >
                    <span
                        class="w-1 h-4 bg-gradient-to-b from-blue-500 to-purple-600 rounded-full"
                    ></span>
                    布局设置
                </h3>
                <div class="space-y-4">
                    <div
                        class="flex items-center justify-between p-4 bg-white dark:bg-gray-800 rounded-xl shadow-sm"
                    >
                        <div class="flex items-center gap-3">
                            <el-icon :size="20" class="text-blue-500">
                                <Location />
                            </el-icon>
                            <div>
                                <div class="text-sm font-medium text-gray-700 dark:text-gray-200">
                                    面包屑导航
                                </div>
                                <div class="text-xs text-gray-500 dark:text-gray-400">
                                    显示页面路径导航
                                </div>
                            </div>
                        </div>
                        <el-switch
                            v-model="showBreadcrumb"
                            @change="handleBreadcrumbChange"
                            active-color="#409EFF"
                            inactive-color="#dcdfe6"
                        />
                    </div>

                    <div
                        class="flex items-center justify-between p-4 bg-white dark:bg-gray-800 rounded-xl shadow-sm"
                    >
                        <div class="flex items-center gap-3">
                            <el-icon :size="20" class="text-blue-500">
                                <Bottom />
                            </el-icon>
                            <div>
                                <div class="text-sm font-medium text-gray-700 dark:text-gray-200">
                                    页脚显示
                                </div>
                                <div class="text-xs text-gray-500 dark:text-gray-400">
                                    显示页面底部信息
                                </div>
                            </div>
                        </div>
                        <el-switch
                            v-model="showFooter"
                            @change="handleFooterChange"
                            active-color="#409EFF"
                            inactive-color="#dcdfe6"
                        />
                    </div>
                </div>
            </section>

            <section class="mt-8 pt-6 border-t border-gray-200 dark:border-gray-700">
                <h3
                    class="text-sm font-bold text-gray-700 dark:text-gray-300 mb-4 uppercase tracking-widest flex items-center gap-2"
                >
                    <span
                        class="w-1 h-4 bg-gradient-to-b from-blue-500 to-purple-600 rounded-full"
                    ></span>
                    字体大小
                </h3>
                <div class="p-4 bg-white dark:bg-gray-800 rounded-xl shadow-sm">
                    <div class="flex items-center justify-between mb-4">
                        <div class="flex items-center gap-3">
                            <el-icon :size="20" class="text-blue-500">
                                <FontSize />
                            </el-icon>
                            <div>
                                <div class="text-sm font-medium text-gray-700 dark:text-gray-200">
                                    基础字体大小
                                </div>
                                <div class="text-xs text-gray-500 dark:text-gray-400">
                                    调整全局字体大小
                                </div>
                            </div>
                        </div>
                        <div
                            class="px-3 py-1 bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 rounded-full text-sm font-medium"
                        >
                            {{ fontSize }}px
                        </div>
                    </div>
                    <el-slider
                        v-model="fontSize"
                        :min="12"
                        :max="20"
                        :step="1"
                        @change="handleFontSizeChange"
                        show-stops
                        :marks="{ 12: '小', 14: '中', 16: '大', 18: '特大', 20: '超大' }"
                    />
                    <div class="flex justify-between mt-2 text-xs text-gray-400">
                        <span>12px</span>
                        <span>14px</span>
                        <span>16px</span>
                        <span>18px</span>
                        <span>20px</span>
                    </div>
                </div>
            </section>
        </div>
    </el-drawer>
</template>

<script setup>
import { computed, ref } from 'vue'
import { Sunny, Moon, Check, Location, Bottom } from '@element-plus/icons-vue'
import { useThemeStore } from '@/stores/modules/theme'
import { ElMessage } from 'element-plus'

const props = defineProps({
    visible: {
        type: Boolean,
        default: false,
    },
})

const emit = defineEmits(['update:visible'])

const themeStore = useThemeStore()

const drawerVisible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val),
})

const showBreadcrumb = computed({
    get: () => themeStore.getShowBreadcrumb,
    set: (val) => themeStore.setShowBreadcrumb(val),
})

const showFooter = computed({
    get: () => themeStore.getShowFooter,
    set: (val) => themeStore.setShowFooter(val),
})

const fontSize = computed({
    get: () => themeStore.getFontSize,
    set: (val) => themeStore.setFontSize(val),
})

const toggleTheme = (value) => {
    themeStore.setMode(value ? 'dark' : 'light')
    ElMessage.success(`已切换到${themeStore.isDark ? '暗色' : '亮色'}模式`)
}

const selectTheme = (key) => {
    themeStore.setThemeKey(key)
    ElMessage.success(`已切换到${themeStore.currentTheme?.label}主题`)
}

const handleBreadcrumbChange = (value) => {
    themeStore.setShowBreadcrumb(value)
    ElMessage.success(value ? '已显示面包屑导航' : '已隐藏面包屑导航')
}

const handleFooterChange = (value) => {
    themeStore.setShowFooter(value)
    ElMessage.success(value ? '已显示页脚' : '已隐藏页脚')
}

const handleFontSizeChange = (value) => {
    themeStore.setFontSize(value)
    ElMessage.success(`字体大小已设置为 ${value}px`)
}
</script>

<style scoped>
.theme-drawer :deep(.el-drawer__header) {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border-radius: 8px 8px 0 0;
}

.theme-drawer :deep(.el-drawer__title) {
    color: white;
    font-weight: 600;
}

.theme-drawer :deep(.el-drawer__headerbtn .el-drawer__close) {
    color: white;
}

.theme-drawer :deep(.el-drawer__body) {
    padding: 24px;
}
</style>
