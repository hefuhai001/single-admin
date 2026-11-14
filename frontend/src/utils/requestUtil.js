import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/modules/auth.js'
import { handleTokenExpired } from '@/utils/tokenRefresh.js'

const apiUrl = import.meta.env.VITE_API_URL

// 创建 axios 实例
const request = axios.create({
    baseURL: apiUrl,
    timeout: 60000,
})

// 请求拦截器
request.interceptors.request.use((config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
        config.headers['Authorization'] = 'Bearer ' + authStore.token
        config.headers['X-Device-Id'] = authStore.deviceId || ''
    }
    config.headers['Accept'] = 'application/json, text/plain, */*'
    return config
})

// 响应拦截器
request.interceptors.response.use(
    (response) => {
        // 二进制响应（如文件下载）直接返回，不按业务状态码处理
        if (response.config.responseType === 'blob') {
            return response
        }

        const resData = response.data
        if (!resData) return response

        // 后端 SaResult 返回 HTTP 200，但 body 中 code 标识业务状态
        if (resData.code === 401) {
            return handleTokenExpired(response.config).then((config) => request(config))
        }

        if (resData.code !== 200) {
            ElMessage.error(resData.msg || '请求失败')
            return Promise.reject(new Error(resData.msg || '请求失败'))
        }

        return response
    },
    (error) => {
        const { response } = error

        // 网络异常等没有响应时直接抛出，不重复提示
        if (!response) {
            return Promise.reject(error)
        }

        const resData = response.data
        // 未登录 / token 过期：HTTP 401 或响应体 code 401 都走静默刷新
        if (response.status === 401 || (resData && resData.code === 401)) {
            return handleTokenExpired(response.config).then((config) => request(config))
        }

        if (response.status === 404) {
            ElMessage.error({ message: '404 not found', duration: 1000, showClose: true })
            return Promise.reject(error)
        }

        // 异常响应体仍是 SaResult，优先展示业务消息
        const message = (resData && resData.msg) || error.message || '请求失败'
        ElMessage.error(message)
        return Promise.reject(error)
    },
)

export default request
