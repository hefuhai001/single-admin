/**
 * v-throttle
 * 节流指令，用于限制事件触发频率
 * 接收参数：function类型/Object类型（包含handler和delay）
 * @example
 * <template>
 *  <button v-throttle：click="handleClick">点击</button>
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

const throttle = {
    mounted(el, binding) {
        // 处理两种传参方式：直接传函数 或 传配置对象
        if (typeof binding.value === 'function') {
            el.throttleHandler = throttleFn(binding.value, 1000)
        } else {
            const { handler, delay = 1000, leading = true, trailing = false } = binding.value
            el.throttleHandler = throttleFn(handler, delay, leading, trailing)
        }

        // 默认监听click事件，可通过修饰符指定其他事件
        const event = binding.arg || 'click'
        el.addEventListener(event, el.throttleHandler)
    },
    updated(el, binding) {
        // 更新时重新创建节流函数
        if (typeof binding.value === 'function') {
            el.throttleHandler = throttleFn(binding.value, 1000)
        } else {
            const { handler, delay = 1000, leading = true, trailing = false } = binding.value
            el.throttleHandler = throttleFn(handler, delay, leading, trailing)
        }
    },
    beforeUnmount(el, binding) {
        const event = binding.arg || 'click'
        el.removeEventListener(event, el.throttleHandler)
    },
}

/**
 * 节流函数
 * @param {Function} func - 要节流的函数
 * @param {number} delay - 延迟时间，默认1000ms
 * @param {boolean} leading - 是否在开始时立即执行，默认true
 * @param {boolean} trailing - 是否在结束时执行，默认false
 * @returns {Function} 节流后的函数
 */
function throttleFn(func, delay, leading = true, trailing = false) {
    let lastTime = 0
    let timeoutId

    return function (...args) {
        const now = Date.now()

        // 首次执行
        if (!lastTime && leading) {
            lastTime = now
            func.apply(this, args)
            return
        }

        // 超过延迟时间，执行函数
        if (now - lastTime >= delay) {
            if (timeoutId) {
                clearTimeout(timeoutId)
                timeoutId = null
            }
            lastTime = now
            func.apply(this, args)
        } else if (trailing && !timeoutId) {
            // 延迟执行
            timeoutId = setTimeout(
                () => {
                    lastTime = leading ? Date.now() : 0
                    timeoutId = null
                    func.apply(this, args)
                },
                delay - (now - lastTime),
            )
        }
    }
}

export default throttle
