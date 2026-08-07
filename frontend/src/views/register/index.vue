<template>
    <div class="relative min-h-screen flex items-center justify-center overflow-hidden bg-gradient-to-br from-[#f0f5ff] via-[#e8f0fe] to-[#f5f3ff] dark:from-[#0f172a] dark:via-[#1e1b4b] dark:to-[#0f172a]">
        <GeoBackground />

        <div class="relative z-10 w-[460px] max-w-[95vw] rounded-[20px] bg-white p-10 px-8 shadow-[0_20px_60px_rgba(0,0,0,0.08),0_4px_16px_rgba(0,0,0,0.04)] animate-[card-enter_0.7s_cubic-bezier(0.16,1,0.3,1)] dark:bg-[#1e293b] dark:shadow-[0_20px_60px_rgba(0,0,0,0.3),0_4px_16px_rgba(0,0,0,0.2)]">
            <div class="mb-8 text-center">
                <h2 class="text-[1.75rem] font-bold tracking-tight text-[#1e293b] dark:text-[#e2e8f0]">创建账户</h2>
                <p class="mt-2 text-[0.95rem] text-[#94a3b8]">请填写信息完成注册</p>
            </div>

            <el-alert
                v-if="!registerEnabled"
                title="注册功能已关闭"
                type="warning"
                :closable="false"
                class="mb-4"
            />

            <el-form
                ref="registerFormRef"
                :model="registerForm"
                :rules="registerRules"
                class="space-y-5"
                @keyup.enter="handleRegister"
            >
                <el-form-item prop="username">
                    <el-input
                        v-model="registerForm.username"
                        placeholder="请输入用户名"
                        size="large"
                        prefix-icon="User"
                    />
                </el-form-item>

                <el-form-item prop="nickname">
                    <el-input
                        v-model="registerForm.nickname"
                        placeholder="请输入昵称"
                        size="large"
                        prefix-icon="UserFilled"
                    />
                </el-form-item>

                <el-form-item prop="email">
                    <el-input
                        v-model="registerForm.email"
                        placeholder="请输入邮箱"
                        size="large"
                        prefix-icon="Message"
                    />
                </el-form-item>

                <el-form-item v-if="verifyCodeEnabled" prop="verifyCode">
                    <div class="flex gap-2 w-full">
                        <el-input
                            v-model="registerForm.verifyCode"
                            placeholder="请输入邮箱验证码"
                            size="large"
                            prefix-icon="Lock"
                            class="flex-1"
                        />
                        <el-button
                            size="large"
                            :disabled="codeCountdown > 0 || !registerForm.email"
                            :loading="codeLoading"
                            @click="handleSendCode"
                        >
                            {{ codeButtonText }}
                        </el-button>
                    </div>
                </el-form-item>

                <el-form-item prop="password">
                    <el-input
                        v-model="registerForm.password"
                        type="password"
                        placeholder="请输入密码"
                        size="large"
                        prefix-icon="Lock"
                        show-password
                    />
                </el-form-item>

                <el-form-item prop="confirmPassword">
                    <el-input
                        v-model="registerForm.confirmPassword"
                        type="password"
                        placeholder="请确认密码"
                        size="large"
                        prefix-icon="Lock"
                        show-password
                    />
                </el-form-item>

                <el-form-item prop="phone">
                    <el-input
                        v-model="registerForm.phone"
                        placeholder="请输入手机号（选填）"
                        size="large"
                        prefix-icon="Phone"
                    />
                </el-form-item>

                <el-form-item class="mb-0">
                    <el-button
                        type="primary"
                        size="large"
                        class="w-full"
                        :disabled="!registerEnabled"
                        :loading="loading"
                        v-throttle:click="handleRegister"
                    >
                        注 册
                    </el-button>
                </el-form-item>
            </el-form>

            <div class="mt-6 text-center text-sm text-[#64748b] dark:text-[#94a3b8]">
                <p>
                    已有账户？
                    <el-button type="primary" link @click="goToLogin">去登录</el-button>
                </p>
            </div>

            <SliderCaptcha v-model="captchaVisible" @success="handleCaptchaSuccess" />
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useConfigStore } from '@/stores/modules/config.js'
import { useAuthStore } from '@/stores/modules/auth.js'
import { useRegisterForm } from '@/composables/useRegister.js'
import GeoBackground from '@/views/login/components/GeoBackground.vue'
import SliderCaptcha from '@/views/register/components/SliderCaptcha.vue'

const router = useRouter()
const configStore = useConfigStore()
const authStore = useAuthStore()

const {
    registerFormRef,
    registerForm,
    registerRules,
    loading,
    registerEnabled,
    codeLoading,
    codeCountdown,
    codeButtonText,
    captchaVisible,
    openCaptcha,
    handleCaptchaSuccess,
    submitRegister,
} = useRegisterForm()

// 是否开启验证码
const verifyCodeEnabled = computed(() => configStore.isEnabled('register.verify_code.enable'))

const handleSendCode = () => {
    if (!registerForm.email) {
        ElMessage.warning('请先填写邮箱')
        return
    }
    openCaptcha()
}

const handleRegister = async () => {
    if (!registerEnabled.value) {
        ElMessage.warning('注册功能已关闭')
        return
    }

    const success = await submitRegister()
    if (success) {
        // 注册成功后拿到 token 并跳转到首页
        await authStore.getProfile()
        await router.push('/admin')
    }
}

const goToLogin = () => {
    router.push('/')
}
</script>

<style scoped>
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
</style>
