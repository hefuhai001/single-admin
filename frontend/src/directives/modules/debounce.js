/**
 * v-debounce
 * 防抖指令，用于限制事件触发频率
 * 接收参数：function类型/Object类型（包含handler和delay）
 * @example
 * <template>
 *  <button v-debounce：click="handleClick">点击</button>
 * </template>
 * <script>
 *  export default {
 *      methods: {
 *          handleClick() {
 *              console.log('点击事件触发')
 *          }
 *      }
 *  }
 * </script>
 */

const debounce = {
    mounted(el, binding) {
        // 处理两种传参方式：直接传函数 或 传配置对象
        if (typeof binding.value === 'function') {
            el.debounceHandler = debounceFn(binding.value, 1000)
        } else {
            const { handler, delay = 1000 } = binding.value
            el.debounceHandler = debounceFn(handler, delay)
        }

        // 默认监听input事件，可通过修饰符指定其他事件
        const event = binding.arg || 'input'
        el.addEventListener(event, el.debounceHandler)
    },
    updated(el, binding) {
        // 更新时重新创建防抖函数
        if (typeof binding.value === 'function') {
            el.debounceHandler = debounceFn(binding.value, 1000)
        } else {
            const { handler, delay = 1000 } = binding.value
            el.debounceHandler = debounceFn(handler, delay)
        }
    },
    beforeUnmount(el, binding) {
        const event = binding.arg || 'input'
        el.removeEventListener(event, el.debounceHandler)
    },
}

/**
 * 防抖函数
 * @param {Function} func - 要防抖的函数
 * @param {number} delay - 延迟时间，默认1000ms
 * @returns {Function} 防抖后的函数
 */
function debounceFn(func, delay) {
    let timeoutId
    return function (...args) {
        clearTimeout(timeoutId)
        timeoutId = setTimeout(() => {
            func.apply(this, args)
        }, delay)
    }
}

export default debounce
