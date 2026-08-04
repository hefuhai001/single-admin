<template>
    <div class="form-side">
        <div class="form-wrapper">
            <div class="form-header">
                <h2 class="form-title">欢迎回来</h2>
                <p class="form-desc">请登录您的账户</p>
            </div>

            <el-form
                ref="loginFormRef"
                :model="loginForm"
                :rules="loginRules"
                class="login-form"
                @keyup.enter="handleLogin"
            >
                <el-form-item prop="username">
                    <el-input
                        v-model="loginForm.username"
                        placeholder="请输入用户名"
                        size="large"
                        prefix-icon="User"
                        class="login-input"
                    />
                </el-form-item>

                <el-form-item prop="password">
                    <el-input
                        v-model="loginForm.password"
                        type="password"
                        placeholder="请输入密码"
                        size="large"
                        prefix-icon="Lock"
                        show-password
                        class="login-input"
                    />
                </el-form-item>

                <el-form-item>
                    <el-button
                        type="primary"
                        size="large"
                        class="login-button w-full"
                        v-throttle:click="handleLogin"
                        :loading="loading"
                    >
                        登 录
                    </el-button>
                </el-form-item>
            </el-form>

            <div class="form-footer">
                <p>&copy; 2024 大数据问卷系统. All rights reserved.</p>
            </div>
        </div>
    </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

defineProps({
    loading: {
        type: Boolean,
        default: false,
    },
})

const emit = defineEmits(['submit'])

const loginFormRef = ref()

const loginForm = reactive({
    username: 'admin',
    password: '123456',
})

const loginRules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 30, message: '密码长度在 6 到 30 个字符', trigger: 'blur' },
    ],
}

const handleLogin = async () => {
    const valid = await loginFormRef.value.validate().catch(() => false)
    if (!valid) {
        ElMessage.error('请填写正确的登录信息')
        return false
    }
    emit('submit', { ...loginForm })
}
</script>

<style scoped>
.form-side {
    flex: 1;
    padding: 3rem 3.5rem;
    background: white;
    display: flex;
    align-items: center;
    justify-content: center;
}

.dark .form-side {
    background: #1e293b;
}

.form-wrapper {
    width: 100%;
    max-width: 360px;
    animation: form-enter 0.7s cubic-bezier(0.16, 1, 0.3, 1) 0.15s both;
}

@keyframes form-enter {
    from {
        opacity: 0;
        transform: translateY(16px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.form-header {
    margin-bottom: 2.5rem;
}

.form-title {
    font-size: 1.75rem;
    font-weight: 700;
    color: #1e293b;
    margin: 0 0 0.5rem;
    letter-spacing: -0.02em;
}

.dark .form-title {
    color: #e2e8f0;
}

.form-desc {
    font-size: 0.95rem;
    color: #94a3b8;
    margin: 0;
}

.login-form :deep(.el-form-item) {
    margin-bottom: 22px;
}

.login-input :deep(.el-input__wrapper) {
    padding: 6px 15px;
    border-radius: 10px;
    box-shadow: 0 0 0 1px #e2e8f0;
    transition: all 0.3s;
}

.dark .login-input :deep(.el-input__wrapper) {
    box-shadow: 0 0 0 1px #334155;
    background: #0f172a;
}

.login-input :deep(.el-input__wrapper:hover) {
    box-shadow: 0 0 0 1px var(--el-color-primary-light-5);
}

.login-input :deep(.el-input__wrapper.is-focus) {
    box-shadow:
        0 0 0 1px var(--el-color-primary),
        0 0 0 3px var(--el-color-primary-light-8);
}

.dark .login-input :deep(.el-input__wrapper.is-focus) {
    box-shadow:
        0 0 0 1px var(--el-color-primary),
        0 0 0 3px rgba(59, 130, 246, 0.15);
}

.login-input :deep(.el-input__inner) {
    height: 42px;
}

.login-button {
    height: 48px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 10px;
    border: none;
    letter-spacing: 0.1em;
    transition: all 0.3s;
}

.login-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(59, 130, 246, 0.35);
}

.login-button:active {
    transform: translateY(0);
}

.form-footer {
    margin-top: 2rem;
    text-align: center;
    font-size: 0.8rem;
    color: #cbd5e1;
}

.dark .form-footer {
    color: #475569;
}

@media (max-width: 768px) {
    .form-side {
        padding: 2rem;
    }

    .form-wrapper {
        max-width: 100%;
    }
}

@media (max-width: 480px) {
    .form-title {
        font-size: 1.4rem;
    }
}
</style>
