<template>
    <el-container>
        <el-aside :width="asideWidth" class="border-r dark:border-gray-600">
            <AsideCom />
        </el-aside>
        <el-container class="relative">
            <el-header style="padding: 0; height: 90px" class="border-b dark:border-gray-600">
                <HeaderCom />
                <TabsCom />
            </el-header>
            <el-main
                class="bg-[#f1f2f3] dark:bg-[#1d1e1f]"
                :style="{
                    height: showFooter ? 'calc(100vh - 150px - 24px)' : 'calc(100vh - 150px)',
                    padding: 0,
                }"
            >
                <OverlayScrollbarsComponent
                    defer
                    class="h-full p-2.5"
                    :options="{ scrollbars: { autoHide: 'leave', autoHideDelay: 300 } }"
                >
                    <router-view v-slot="{ Component }">
                        <transition name="route-fade" mode="out-in">
                            <component :is="Component" />
                        </transition>
                    </router-view>
                </OverlayScrollbarsComponent>
            </el-main>
            <el-footer
                v-if="showFooter"
                height="24px"
                class="border-t dark:border-gray-600 dark:bg-[#1d1e1f]"
            >
                <FooterCom />
            </el-footer>
        </el-container>
    </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useThemeStore } from '@/stores/modules/theme'
import { OverlayScrollbarsComponent } from 'overlayscrollbars-vue'
import AsideCom from '@/views/admin/layout/AsideCom.vue'
import FooterCom from '@/views/admin/layout/FooterCom.vue'
import HeaderCom from '@/views/admin/layout/HeaderCom.vue'
import TabsCom from '@/views/admin/layout/TabsCom.vue'

const themeStore = useThemeStore()

const asideWidth = computed(() => {
    return themeStore.getSidebarCollapsed ? '64px' : '200px'
})

const showFooter = computed(() => {
    return themeStore.getShowFooter
})
</script>

<style scoped>
:deep(.el-aside) {
    height: 100vh;
    transition: width 0.3s ease;
}

/* 路由切换淡入淡出动画 */
.route-fade-enter-active,
.route-fade-leave-active {
    transition: opacity 0.2s ease;
}

.route-fade-enter-from,
.route-fade-leave-to {
    opacity: 0;
}
</style>
