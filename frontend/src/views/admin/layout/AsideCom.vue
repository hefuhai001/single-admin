<template>
    <div class="aside-layout h-full flex flex-col">
        <!-- 固定顶部 Logo -->
        <div class="logo-area flex items-center px-4 py-2 shrink-0 bg-white dark:bg-[#1d1e1f]">
            <svg-icon name="logo" class="logo-icon" />
            <span
                v-show="!themeStore.getSidebarCollapsed"
                class="font-bold text-xl ml-2 text-theme-500 whitespace-nowrap"
            >
                大数据问卷系统
            </span>
        </div>

        <!-- 可滚动菜单区 -->
        <OverlayScrollbarsComponent
            defer
            class="flex-1 min-h-0 menu-scroll-area"
            :options="{ scrollbars: { autoHide: 'leave', autoHideDelay: 300 } }"
        >
            <el-menu
                :default-active="activeMenu"
                :collapse="themeStore.getSidebarCollapsed"
                router
                class="border-r-0"
                :collapse-transition="false"
                :unique-opened="true"
            >
                <template v-for="route in dynamicRoutes" :key="route.path">
                    <el-sub-menu
                        v-if="route.children && route.children.length > 0"
                        :index="route.path"
                    >
                        <template #title>
                            <el-icon v-if="route.meta?.icon">
                                <component :is="getIcon(route.meta.icon)" />
                            </el-icon>
                            <span>{{ route.meta?.title }}</span>
                        </template>
                        <el-menu-item
                            v-for="child in route.children"
                            :key="child.path"
                            :index="child.path"
                        >
                            <el-icon v-if="child.meta?.icon">
                                <component :is="getIcon(child.meta.icon)" />
                            </el-icon>
                            <span>{{ child.meta?.title }}</span>
                        </el-menu-item>
                    </el-sub-menu>

                    <el-menu-item v-else :index="route.path">
                        <el-icon v-if="route.meta?.icon">
                            <component :is="getIcon(route.meta.icon)" />
                        </el-icon>
                        <span>{{ route.meta?.title }}</span>
                    </el-menu-item>
                </template>
            </el-menu>
        </OverlayScrollbarsComponent>
    </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useRouterStore } from '@/stores/modules/router'
import { useThemeStore } from '@/stores/modules/theme'
import { OverlayScrollbarsComponent } from 'overlayscrollbars-vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import SvgIcon from '@/components/SvgIcon.vue'

const route = useRoute()
const routerStore = useRouterStore()
const themeStore = useThemeStore()

const dynamicRoutes = computed(() => {
    return routerStore.dynamicRoutes
})

const activeMenu = computed(() => {
    return route.path
})

function getIcon(iconName) {
    return ElementPlusIconsVue[iconName] || ElementPlusIconsVue.Menu
}
</script>

<style scoped>
.aside-layout {
    overflow: hidden;
}

.logo-area {
    border-bottom: 1px solid var(--el-menu-border-color);
}

.logo-icon {
    width: 40px;
    height: 40px;
    flex-shrink: 0;
}

:deep(.el-menu) {
    border-right: none;
    min-height: 100%;
}

/* 统一滚动区背景色，避免菜单未撑满时下方出现色差 */
.menu-scroll-area {
    background: var(--el-menu-bg-color);
}

:deep(.el-sub-menu__title),
:deep(.el-menu-item) {
    display: flex;
    align-items: center;
}

:deep(.el-icon) {
    width: 18px;
    height: 18px;
    margin-right: 8px;
}
</style>
