<template>
    <el-dialog
        v-model="dialogVisible"
        title="请完成安全验证"
        width="340"
        align-center
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="!verifying"
        @closed="handleClosed"
    >
        <div class="select-none">
            <!-- 图片区域 -->
            <div
                ref="imageRef"
                class="relative mx-auto mb-3 h-[155px] w-[280px] overflow-hidden rounded-lg bg-gray-100"
            >
                <div v-if="loading" class="flex h-full w-full items-center justify-center">
                    <el-icon class="animate-spin text-2xl text-primary">
                        <Loading />
                    </el-icon>
                </div>
                <template v-else>
                    <img
                        v-if="backgroundImage"
                        :src="backgroundImage"
                        class="h-full w-full select-none"
                        draggable="false"
                    />
                    <img
                        v-if="sliderImage"
                        :src="sliderImage"
                        class="absolute h-[50px] w-[50px] select-none"
                        :style="{ top: sliderY + 'px', left: sliderLeft + 'px' }"
                        draggable="false"
                    />
                </template>
            </div>

            <!-- 滑动条 -->
            <div
                ref="trackRef"
                class="relative mx-auto h-10 w-[280px] overflow-hidden rounded-full bg-gray-100"
            >
                <div
                    class="absolute left-0 top-0 h-full bg-primary/10"
                    :style="{ width: sliderLeft + 'px' }"
                ></div>
                <div
                    class="absolute top-0.5 flex h-9 w-9 cursor-grab items-center justify-center rounded-full bg-gradient-to-br from-primary to-orange-400 text-white shadow-md active:cursor-grabbing"
                    :style="{ left: sliderLeft + 'px' }"
                    @mousedown="startDrag"
                    @touchstart.prevent="startDrag"
                >
                    <el-icon v-if="!verifying"><DArrowRight /></el-icon>
                    <el-icon v-else class="animate-spin"><Loading /></el-icon>
                </div>
                <span
                    v-if="sliderLeft < 10"
                    class="pointer-events-none absolute inset-0 flex items-center justify-center text-sm text-gray-400"
                >
                    {{ dragHint }}
                </span>
            </div>

            <!-- 刷新 -->
            <div class="mx-auto mt-2 w-[280px] text-right">
                <el-button type="primary" link size="small" @click="refreshCaptcha">
                    <el-icon class="mr-1"><Refresh /></el-icon>
                    换一张
                </el-button>
            </div>
        </div>
    </el-dialog>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { Loading, DArrowRight, Refresh } from '@element-plus/icons-vue'
import api from '@/api/index.js'
import { ElMessage } from 'element-plus'

const props = defineProps({
    modelValue: { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue', 'success'])

const dialogVisible = ref(false)
const loading = ref(false)
const verifying = ref(false)
const backgroundImage = ref('')
const sliderImage = ref('')
const sliderY = ref(0)
const sliderLeft = ref(0)
const token = ref('')
const dragging = ref(false)
const startX = ref(0)
const trackRef = ref()
const imageRef = ref()
const dragHint = ref('向右滑动滑块填充拼图')

watch(
    () => props.modelValue,
    (val) => {
        dialogVisible.value = val
        if (val) fetchCaptcha()
    },
)

watch(dialogVisible, (val) => {
    emit('update:modelValue', val)
})

// 获取验证码
async function fetchCaptcha() {
    loading.value = true
    sliderLeft.value = 0
    dragHint.value = '向右滑动滑块填充拼图'
    try {
        const { data } = await api.captcha.getSliderCaptcha()
        token.value = data.data.token
        backgroundImage.value = data.data.backgroundImage
        sliderImage.value = data.data.sliderImage
        sliderY.value = data.data.sliderY
    } catch {
        dragHint.value = '加载失败，请重试'
    } finally {
        loading.value = false
    }
}

// 刷新
function refreshCaptcha() {
    fetchCaptcha()
}

// 拖拽开始
function startDrag(e) {
    if (loading.value || verifying.value) return
    dragging.value = true
    const clientX = e.touches ? e.touches[0].clientX : e.clientX
    startX.value = clientX - sliderLeft.value
    dragHint.value = ''
}

// 拖拽移动
function onDrag(e) {
    if (!dragging.value) return
    const clientX = e.touches ? e.touches[0].clientX : e.clientX
    let newLeft = clientX - startX.value
    const maxLeft = 280 - 50
    newLeft = Math.max(0, Math.min(newLeft, maxLeft))
    sliderLeft.value = newLeft
}

// 拖拽结束
async function endDrag() {
    if (!dragging.value) return
    dragging.value = false

    if (sliderLeft.value < 10) {
        sliderLeft.value = 0
        dragHint.value = '向右滑动滑块填充拼图'
        return
    }

    verifying.value = true
    try {
        const { data } = await api.captcha.verifySlider({
            token: token.value,
            position: Math.round(sliderLeft.value),
        })
        emit('success', data.data)
        dialogVisible.value = false
    } catch (error) {
        sliderLeft.value = 0
        dragHint.value = error?.response?.data?.msg || '验证失败，请重试'
        setTimeout(() => fetchCaptcha(), 1000)
    } finally {
        verifying.value = false
    }
}

function handleClosed() {
    sliderLeft.value = 0
    dragHint.value = '向右滑动滑块填充拼图'
}

onMounted(() => {
    document.addEventListener('mousemove', onDrag)
    document.addEventListener('mouseup', endDrag)
    document.addEventListener('touchmove', onDrag, { passive: false })
    document.addEventListener('touchend', endDrag)
})

onBeforeUnmount(() => {
    document.removeEventListener('mousemove', onDrag)
    document.removeEventListener('mouseup', endDrag)
    document.removeEventListener('touchmove', onDrag)
    document.removeEventListener('touchend', endDrag)
})
</script>

<style scoped>
.text-primary {
    color: var(--el-color-primary);
}
.bg-primary\/10 {
    background-color: var(--el-color-primary);
    opacity: 0.1;
}
.from-primary {
    --tw-gradient-from: var(--el-color-primary) var(--tw-gradient-from-position);
    --tw-gradient-to: rgb(255 255 255 / 0) var(--tw-gradient-to-position);
    --tw-gradient-stops: var(--tw-gradient-from), var(--tw-gradient-to);
}
</style>
