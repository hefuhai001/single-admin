import { ref } from 'vue'
import { defineStore } from 'pinia'
import api from '@/api/index.js'
import { ElMessage } from 'element-plus'
import { resetRouterState } from '@/router/index.js'

export const useAuthStore = defineStore(
    'auth',
    () => {
        const token = ref('')
        const refreshToken = ref('')
        const userInfo = ref({})

        async function login(loginData) {
            try {
                const res = await api.auth.login(loginData.username, loginData.password)
                if (res.data && res.data.data) {
                    const data = res.data.data
                    // 双 token 模式：后端返回 { accessToken, refreshToken }
                    token.value = data.accessToken
                    refreshToken.value = data.refreshToken
                    ElMessage({
                        type: res.data.code === 200 ? 'success' : 'error',
                        message: res.data.msg,
                    })
                    await getProfile()
                    return true
                }
                return false
            } catch (error) {
                console.error('登录失败:', error)
                throw error
            }
        }

        async function register(registerData) {
            try {
                const res = await api.auth.register(registerData)
                if (res.data && res.data.data) {
                    const data = res.data.data
                    // 注册成功后后端直接登录，返回双 token
                    token.value = data.accessToken
                    refreshToken.value = data.refreshToken
                    ElMessage.success('注册成功')
                    await getProfile()
                    return true
                }
                return false
            } catch (error) {
                console.error('注册失败:', error)
                throw error
            }
        }

        async function logout() {
            try {
                await api.auth.logout()
            } catch (error) {
                console.error('退出登录失败:', error)
            } finally {
                clearAuth()
            }
        }

        async function getProfile() {
            try {
                const res = await api.auth.profile()
                if (res.data && res.data.data) {
                    userInfo.value = res.data.data
                    return userInfo.value
                }
                return null
            } catch (error) {
                console.error('获取用户信息失败:', error)
                throw error
            }
        }

        function clearAuth() {
            token.value = ''
            refreshToken.value = ''
            userInfo.value = {}
            resetRouterState()
        }

        function setUserInfo(info) {
            userInfo.value = info
        }

        function getUserInfo() {
            return userInfo.value
        }

        return {
            token,
            refreshToken,
            userInfo,
            login,
            register,
            logout,
            getUserInfo,
            clearAuth,
            setUserInfo,
            getProfile,
        }
    },
    {
        persist: {
            key: 'auth',
            paths: ['token', 'refreshToken', 'userInfo'],
        },
    },
)
