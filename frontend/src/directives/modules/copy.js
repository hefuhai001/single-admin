/**
 * v-copy
 * 复制某个值至剪贴板
 * 接收参数：string类型/Ref<string>类型/Reactive<string>类型
 * @example
 * <template>
 *  <div v-copy="copyText">点击复制</div>
 * </template>
 * <script>
 *  export default {
 *      data() {
 *          return {
 *              copyText: '复制的文本'
 *          }
 *      }
 *  }
 * </script>
 */

import { ElMessage } from 'element-plus'

const copy = {
    mounted(el, binding) {
        el.copyData = binding.value
        el.addEventListener('click', handleClick)
    },
    updated(el, binding) {
        el.copyData = binding.value
    },
    beforeUnmount(el) {
        el.removeEventListener('click', handleClick)
    },
}

async function handleClick() {
    try {
        // 检查是否支持 Clipboard API
        if (!navigator.clipboard) {
            throw new Error('Clipboard API 不被支持')
        }

        // 处理不同类型的绑定值
        const copyValue = typeof this.copyData === 'function' ? this.copyData() : this.copyData

        await navigator.clipboard.writeText(copyValue)
        ElMessage({
            type: 'success',
            message: '复制成功',
        })
    } catch (err) {
        console.error('复制操作失败: ', err)

        // 降级方案：使用传统方式复制
        try {
            const textArea = document.createElement('textarea')
            const copyValue = typeof this.copyData === 'function' ? this.copyData() : this.copyData
            textArea.value = copyValue
            document.body.appendChild(textArea)
            textArea.select()
            document.execCommand('copy')
            document.body.removeChild(textArea)

            ElMessage({
                type: 'success',
                message: '复制成功',
            })
        } catch (fallbackErr) {
            console.error('降级复制方案也失败: ', fallbackErr)
            ElMessage({
                type: 'error',
                message: '复制失败，请手动复制',
            })
        }
    }
}

export default copy
