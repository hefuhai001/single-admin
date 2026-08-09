<template>
    <div class="login-page relative min-h-screen flex flex-col items-center justify-center overflow-hidden">
        <GeoBackground />

        <div class="login-card relative z-10 flex w-[900px] max-w-[95vw] min-h-[540px] rounded-[20px] overflow-hidden shadow-[0_20px_60px_rgba(0,0,0,0.08),0_4px_16px_rgba(0,0,0,0.04)] dark:shadow-[0_20px_60px_rgba(0,0,0,0.3),0_4px_16px_rgba(0,0,0,0.2)]">
            <BrandSide />
            <LoginForm :loading="loading" @submit="handleLogin" @go-register="goToRegister" />
        </div>

        <a
            v-if="icp"
            href="https://beian.miit.gov.cn/"
            target="_blank"
            rel="noopener noreferrer"
            class="absolute bottom-6 left-1/2 -translate-x-1/2 text-center text-xs text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 transition-colors"
        >
            {{ icp }}
        </a>
    </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import router from '@/router/index.js'
import { useAuthStore } from '@/stores/modules/auth.js'
import { useConfigStore } from '@/stores/modules/config'
import GeoBackground from './components/GeoBackground.vue'
import BrandSide from './components/BrandSide.vue'
import LoginForm from './components/LoginForm.vue'

const authStore = useAuthStore()
const configStore = useConfigStore()
const loading = ref(false)

const icp = computed(() => configStore.getValue('site.icp', '粤ICP备2023000000号'))

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
    background: linear-gradient(160deg, #f0f5ff 0%, #e8f0fe 40%, #f5f3ff 100%);
}

.dark .login-page {
    background: linear-gradient(160deg, #0f172a 0%, #1e1b4b 40%, #0f172a 100%);
}

.login-card {
    animation: card-enter 0.7s cubic-bezier(0.16, 1, 0.3, 1);
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
