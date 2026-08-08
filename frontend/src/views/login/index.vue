<template>
    <div class="login-page">
        <GeoBackground />

        <div class="login-card">
            <BrandSide />
            <LoginForm :loading="loading" @submit="handleLogin" @go-register="goToRegister" />
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import router from '@/router/index.js'
import { useAuthStore } from '@/stores/modules/auth.js'
import GeoBackground from './components/GeoBackground.vue'
import BrandSide from './components/BrandSide.vue'
import LoginForm from './components/LoginForm.vue'

const authStore = useAuthStore()
const loading = ref(false)

const handleLogin = async (formData) => {
    try {
        loading.value = true
        if (await authStore.login(formData)) {
            await router.push('/admin')
        }
    } catch (error) {
        // 错误提示统一由响应拦截器处理，此处不再重复弹窗
        console.error('登录失败:', error)
    } finally {
        loading.value = false
    }
}

const goToRegister = () => {
    router.push('/register')
}
</script>

<style scoped>
.login-page {
    position: relative;
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    background: linear-gradient(160deg, #f0f5ff 0%, #e8f0fe 40%, #f5f3ff 100%);
}

.dark .login-page {
    background: linear-gradient(160deg, #0f172a 0%, #1e1b4b 40%, #0f172a 100%);
}

.login-card {
    position: relative;
    z-index: 1;
    display: flex;
    width: 900px;
    max-width: 95vw;
    min-height: 540px;
    border-radius: 20px;
    overflow: hidden;
    box-shadow:
        0 20px 60px rgba(0, 0, 0, 0.08),
        0 4px 16px rgba(0, 0, 0, 0.04);
    animation: card-enter 0.7s cubic-bezier(0.16, 1, 0.3, 1);
}

.dark .login-card {
    box-shadow:
        0 20px 60px rgba(0, 0, 0, 0.3),
        0 4px 16px rgba(0, 0, 0, 0.2);
}

@keyframes card-enter {
    from {
        opacity: 0;
        transform: translateY(30px) scale(0.97);
    }
    to {
        opacity: 1;
        transform: translateY(0) scale(1);
    }
}

@media (max-width: 768px) {
    .login-card {
        flex-direction: column;
        min-height: auto;
    }
}
</style>
