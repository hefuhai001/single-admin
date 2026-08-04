<template>
    <el-row
        type="flex"
        align="middle"
        justify="space-between"
        class="border-b dark:border-neutral-500 bg-white dark:bg-[#1d1e1f]"
    >
        <div class="flex items-center">
            <el-button
                link
                @click="toggleSidebar"
                class="mx-4 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
            >
                <el-icon :size="20">
                    <Fold v-if="!themeStore.getSidebarCollapsed" />
                    <Expand v-else />
                </el-icon>
            </el-button>
            <el-breadcrumb v-if="themeStore.getShowBreadcrumb" separator="/">
                <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path" :to="item.path">
                    {{ item.meta?.title }}
                </el-breadcrumb-item>
            </el-breadcrumb>
        </div>

        <div class="flex items-center space-x-4">
            <el-tooltip :content="isFullscreen ? '退出全屏' : '全屏'" placement="bottom">
                <el-button
                    link
                    @click="toggleFullscreen"
                    class="hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
                >
                    <el-icon :size="20">
                        <FullScreen v-if="!isFullscreen" />
                        <Aim v-else />
                    </el-icon>
                </el-button>
            </el-tooltip>

            <el-tooltip
                :content="themeStore.isDark ? '切换亮色模式' : '切换暗色模式'"
                placement="bottom"
            >
                <el-button
                    link
                    @click="toggleTheme"
                    class="hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
                >
                    <el-icon :size="20">
                        <Sunny v-if="!themeStore.isDark" />
                        <Moon v-else />
                    </el-icon>
                </el-button>
            </el-tooltip>

            <el-tooltip content="主题设置" placement="bottom">
                <el-button
                    link
                    @click="themeConfigVisible = true"
                    class="hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
                >
                    <el-icon :size="20">
                        <Brush />
                    </el-icon>
                </el-button>
            </el-tooltip>

            <el-dropdown @command="handleCommand" trigger="click">
                <div
                    class="mr-4 flex items-center cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-800 rounded px-3 py-2 transition-colors"
                >
                    <el-avatar :size="32" class="mr-2">
                        <el-icon :size="20">
                            <User />
                        </el-icon>
                    </el-avatar>
                    <span class="text-sm text-gray-700 dark:text-gray-200">
                        {{ userInfo.nickname }}
                    </span>
                    <el-icon class="ml-1">
                        <ArrowDown />
                    </el-icon>
                </div>
                <template #dropdown>
                    <el-dropdown-menu>
                        <el-dropdown-item command="profile">
                            <el-icon>
                                <User />
                            </el-icon>
                            个人中心
                        </el-dropdown-item>
                        <el-dropdown-item command="settings">
                            <el-icon>
                                <Setting />
                            </el-icon>
                            系统设置
                        </el-dropdown-item>
                        <el-dropdown-item divided command="logout">
                            <el-icon>
                                <SwitchButton />
                            </el-icon>
                            退出登录
                        </el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
        </div>

        <ThemeConfig v-model:visible="themeConfigVisible" />
        <ProfileDialog v-model="profileVisible" />
        <SettingsDialog v-model="settingsVisible" />
    </el-row>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useThemeStore } from '@/stores/modules/theme'
import { useRouterStore } from '@/stores/modules/router'
import { useAuthStore } from '@/stores/modules/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import screenfull from 'screenfull'
import ThemeConfig from '@/components/ThemeConfig.vue'
import ProfileDialog from '@/components/ProfileDialog.vue'
import SettingsDialog from '@/components/SettingsDialog.vue'
import {
    Aim,
    ArrowDown,
    Expand,
    Fold,
    FullScreen,
    Moon,
    Setting,
    Sunny,
    SwitchButton,
    User,
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const themeStore = useThemeStore()
const routerStore = useRouterStore()

const themeConfigVisible = ref(false)
const profileVisible = ref(false)
const settingsVisible = ref(false)
const isFullscreen = ref(false)

const userInfo = computed(() => authStore.userInfo || {})

const breadcrumbs = computed(() => {
    return route.matched.filter((item) => item.meta?.title)
})

function toggleSidebar() {
    themeStore.toggleSidebar()
}

function toggleTheme() {
    themeStore.toggleMode()
    ElMessage.success(`已切换到${themeStore.isDark ? '暗色' : '亮色'}模式`)
}

function toggleFullscreen() {
    if (!screenfull.isEnabled) {
        ElMessage.warning('您的浏览器不支持全屏功能')
        return
    }
    screenfull.toggle()
}

function changeFullscreen() {
    isFullscreen.value = screenfull.isFullscreen
}

onMounted(() => {
    if (screenfull.isEnabled) {
        screenfull.on('change', changeFullscreen)
    }
})

onUnmounted(() => {
    if (screenfull.isEnabled) {
        screenfull.off('change', changeFullscreen)
    }
})

async function handleCommand(command) {
    switch (command) {
        case 'profile':
            profileVisible.value = true
            break
        case 'settings':
            settingsVisible.value = true
            break
        case 'logout':
            try {
                await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                })
                await authStore.logout()
                routerStore.setDynamicRoutes([])
                await router.push('/')
                ElMessage.success('退出登录成功')
            } catch {
                // 取消退出
            }
            break
    }
}
</script>

<style scoped></style>
