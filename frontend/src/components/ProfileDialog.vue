<template>
    <el-dialog
        v-model="visible"
        title="个人中心"
        width="520px"
        :close-on-click-modal="false"
        destroy-on-close
    >
        <el-tabs v-model="profileTab">
            <el-tab-pane label="基本信息" name="info">
                <el-form
                    ref="profileFormRef"
                    :model="profileForm"
                    :rules="profileRules"
                    label-width="80px"
                    class="mt-4 mr-12"
                >
                    <el-form-item label="用户名">
                        <el-input :model-value="userInfo.username" disabled />
                    </el-form-item>
                    <el-form-item label="角色">
                        <el-input :model-value="roleText" disabled />
                    </el-form-item>
                    <el-form-item label="昵称" prop="nickname">
                        <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
                    </el-form-item>
                    <el-form-item label="邮箱" prop="email">
                        <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
                    </el-form-item>
                    <el-form-item label="手机号" prop="phone">
                        <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
                    </el-form-item>
                </el-form>
            </el-tab-pane>
            <el-tab-pane label="修改密码" name="password">
                <el-form
                    ref="passwordFormRef"
                    :model="passwordForm"
                    :rules="passwordRules"
                    label-width="100px"
                    class="mt-4 mr-12"
                >
                    <el-form-item label="当前密码" prop="oldPassword">
                        <el-input
                            v-model="passwordForm.oldPassword"
                            type="password"
                            show-password
                            placeholder="请输入当前密码"
                        />
                    </el-form-item>
                    <el-form-item label="新密码" prop="newPassword">
                        <el-input
                            v-model="passwordForm.newPassword"
                            type="password"
                            show-password
                            placeholder="请输入新密码"
                        />
                    </el-form-item>
                    <el-form-item label="确认新密码" prop="confirmPassword">
                        <el-input
                            v-model="passwordForm.confirmPassword"
                            type="password"
                            show-password
                            placeholder="请再次输入新密码"
                        />
                    </el-form-item>
                </el-form>
            </el-tab-pane>
        </el-tabs>
        <template #footer>
            <el-button @click="visible = false">取消</el-button>
            <el-button type="primary" :loading="loading" @click="handleSubmit">保存</el-button>
        </template>
    </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api/index.js'
import { useAuthStore } from '@/stores/modules/auth.js'
import { useRouterStore } from '@/stores/modules/router.js'

const props = defineProps({
    modelValue: Boolean,
})

const emit = defineEmits(['update:modelValue'])

const router = useRouter()
const authStore = useAuthStore()
const routerStore = useRouterStore()

const visible = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val),
})

const profileTab = ref('info')
const loading = ref(false)
const profileFormRef = ref()
const passwordFormRef = ref()

const profileForm = ref({
    nickname: '',
    email: '',
    phone: '',
})

const passwordForm = ref({
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
})

const profileRules = {
    nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
    email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }],
    phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
}

const validateConfirmPassword = (rule, value, callback) => {
    if (value !== passwordForm.value.newPassword) {
        callback(new Error('两次输入的密码不一致'))
    } else {
        callback()
    }
}

const passwordRules = {
    oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
    newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, max: 30, message: '密码长度在 6 到 30 个字符', trigger: 'blur' },
    ],
    confirmPassword: [
        { required: true, message: '请再次输入新密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' },
    ],
}

const userInfo = computed(() => authStore.userInfo || {})

const roleText = computed(() => {
    const roles = userInfo.value.roles
    if (!roles) return '普通用户'
    if (Array.isArray(roles)) return roles.join('、') || '普通用户'
    return roles
})

// 打开弹窗时填充表单
watch(visible, (val) => {
    if (val) {
        profileTab.value = 'info'
        profileForm.value = {
            nickname: userInfo.value.nickname || '',
            email: userInfo.value.email || '',
            phone: userInfo.value.phone || '',
        }
        passwordForm.value = {
            oldPassword: '',
            newPassword: '',
            confirmPassword: '',
        }
    }
})

async function handleSubmit() {
    if (profileTab.value === 'info') {
        const valid = await profileFormRef.value?.validate().catch(() => false)
        if (!valid) return

        loading.value = true
        try {
            await api.user.updateUser({
                id: userInfo.value.id,
                nickname: profileForm.value.nickname,
                email: profileForm.value.email,
                phone: profileForm.value.phone,
            })
            await authStore.getProfile()
            ElMessage.success('个人信息更新成功')
            visible.value = false
        } catch {
            ElMessage.error('更新失败，请稍后重试')
        } finally {
            loading.value = false
        }
    } else {
        const valid = await passwordFormRef.value?.validate().catch(() => false)
        if (!valid) return

        loading.value = true
        try {
            await api.user.updateUser({
                id: userInfo.value.id,
                password: passwordForm.value.newPassword,
            })
            ElMessage.success('密码修改成功，请重新登录')
            visible.value = false
            await authStore.logout()
            routerStore.setDynamicRoutes([])
            await router.push('/')
        } catch {
            ElMessage.error('密码修改失败，请稍后重试')
        } finally {
            loading.value = false
        }
    }
}
</script>
