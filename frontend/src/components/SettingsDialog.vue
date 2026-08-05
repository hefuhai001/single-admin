<template>
    <el-dialog
        v-model="visible"
        title="系统设置"
        width="520px"
        :close-on-click-modal="false"
        destroy-on-close
    >
        <el-form label-width="100px" class="mt-4">
            <el-form-item label="主题模式">
                <el-switch
                    :model-value="themeStore.isDark"
                    active-text="暗色"
                    inactive-text="亮色"
                    @change="toggleTheme"
                />
            </el-form-item>
            <el-form-item label="侧边栏折叠">
                <el-switch
                    :model-value="themeStore.getSidebarCollapsed"
                    @change="themeStore.toggleSidebar()"
                />
            </el-form-item>
            <el-form-item label="显示面包屑">
                <el-switch
                    :model-value="themeStore.getShowBreadcrumb"
                    @change="(val) => themeStore.setShowBreadcrumb(val)"
                />
            </el-form-item>
            <el-form-item label="显示页脚">
                <el-switch
                    :model-value="themeStore.getShowFooter"
                    @change="(val) => themeStore.setShowFooter(val)"
                />
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="visible = false">关闭</el-button>
        </template>
    </el-dialog>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useThemeStore } from '@/stores/modules/theme'

const props = defineProps({
    modelValue: Boolean,
})

const emit = defineEmits(['update:modelValue'])

const themeStore = useThemeStore()

const visible = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val),
})

function toggleTheme(value) {
    if (typeof value === 'boolean') {
        themeStore.setMode(value ? 'dark' : 'light')
    } else {
        themeStore.toggleMode()
    }
    ElMessage.success(`已切换到${themeStore.isDark ? '暗色' : '亮色'}模式`)
}
</script>
