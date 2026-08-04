import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/modules/auth.js'
import router from '@/router/index.js'

const apiUrl = import.meta.env.VITE_API_URL

// 是否正在刷新 token
let isRefreshing = false
// 等待刷新的请求队列
let pendingRequests = []

/**
 * 用 refreshToken 刷新 accessToken（轮换模式：同时返回新的 refreshToken）
 * @returns {Promise<string>} 新的 accessToken
 */
export async function refreshAccessToken() {
    const authStore = useAuthStore()
    const res = await axios.post(`${apiUrl}/auth/refresh?refreshToken=${authStore.refreshToken}`)
    if (res.data && res.data.code === 200 && res.data.data) {
        const { accessToken, refreshToken: newRefreshToken } = res.data.data
        authStore.token = accessToken
        authStore.refreshToken = newRefreshToken
        return accessToken
    }
    throw new Error('刷新 token 失败')
}

/**
 * 跳转登录页并清除认证状态
 */
function redirectToLogin(message = '登录已过期，请重新登录') {
    const authStore = useAuthStore()
    authStore.clearAuth()
    router.push('/')
    ElMessage.error(message)
}

/**
 * 处理 token 过期：尝试用 refreshToken 静默刷新
 * @param {object} config - axios request config
 * @returns {Promise} 重试请求的 Promise
 */
export function handleTokenExpired(config) {
    const authStore = useAuthStore()

    // 没有 refreshToken，直接跳转登录
    if (!authStore.refreshToken) {
        redirectToLogin()
        return Promise.reject(new Error('登录已过期'))
    }

    // 如果是刷新接口本身返回 401，说明 refreshToken 也过期了
    if (config.url && config.url.includes('/auth/refresh')) {
        redirectToLogin('refreshToken 已失效，请重新登录')
        return Promise.reject(new Error('refreshToken 已失效'))
    }

    // 正在刷新中，将请求加入队列等待
    if (isRefreshing) {
        return new Promise((resolve) => {
            pendingRequests.push((newToken) => {
                config.headers['Authorization'] = 'Bearer ' + newToken
                resolve(config)
            })
        })
    }

    // 开始刷新
    isRefreshing = true
    return refreshAccessToken()
        .then((newToken) => {
            // 刷新成功，重试所有队列中的请求
            pendingRequests.forEach((cb) => cb(newToken))
            pendingRequests = []
            // 返回更新了 token 的 config，由调用方重新发起请求
            config.headers['Authorization'] = 'Bearer ' + newToken
            return config
        })
        .catch(() => {
            // 刷新失败，清空队列并跳转登录
            pendingRequests = []
            redirectToLogin()
            return Promise.reject(new Error('刷新 token 失败'))
        })
        .finally(() => {
            isRefreshing = false
        })
}
