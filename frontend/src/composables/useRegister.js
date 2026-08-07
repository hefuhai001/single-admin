import { reactive, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api/index.js'
import { useConfigStore } from '@/stores/modules/config.js'

/**
 * 注册表单逻辑
 */
export function useRegisterForm() {
    const configStore = useConfigStore()
    const loading = ref(false)

    // 是否开启注册
    const registerEnabled = computed(() => configStore.isEnabled('register.enable'))

    const registerFormRef = ref()
    const registerForm = reactive({
        username: '',
        password: '',
        confirmPassword: '',
        nickname: '',
        email: '',
        verifyCode: '',
        phone: '',
    })

    const validateConfirmPassword = (rule, value, callback) => {
        if (value !== registerForm.password) {
            callback(new Error('两次输入的密码不一致'))
        } else {
            callback()
        }
    }

    const registerRules = {
        username: [
            { required: true, message: '请输入用户名', trigger: 'blur' },
            { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
        ],
        password: [
            { required: true, message: '请输入密码', trigger: 'blur' },
            { min: 6, max: 30, message: '密码长度在 6 到 30 个字符', trigger: 'blur' },
        ],
        confirmPassword: [
            { required: true, message: '请确认密码', trigger: 'blur' },
            { validator: validateConfirmPassword, trigger: ['blur', 'change'] },
        ],
        nickname: [
            { required: true, message: '请输入昵称', trigger: 'blur' },
            { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' },
        ],
        email: [
            { required: true, message: '请输入邮箱', trigger: 'blur' },
            { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
        ],
        verifyCode: [
            { required: true, message: '请输入验证码', trigger: 'blur' },
            { min: 4, max: 10, message: '验证码长度不正确', trigger: 'blur' },
        ],
        phone: [
            { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
        ],
    }

    const codeLoading = ref(false)
    const codeCountdown = ref(0)
    const codeButtonText = computed(() =>
        codeCountdown.value > 0 ? `${codeCountdown.value}秒后重试` : '获取验证码',
    )

    const sendEmailCode = async () => {
        if (!registerForm.email) {
            ElMessage.warning('请先填写邮箱')
            return
        }
        codeLoading.value = true
        try {
            await api.auth.emailCode(registerForm.email)
            ElMessage.success('验证码已发送')
            codeCountdown.value = 60
            const timer = setInterval(() => {
                codeCountdown.value--
                if (codeCountdown.value <= 0) {
                    clearInterval(timer)
                }
            }, 1000)
        } catch (error) {
            ElMessage.error('验证码发送失败',error)
        } finally {
            codeLoading.value = false
        }
    }

    const submitRegister = async () => {
        if (!registerEnabled.value) {
            ElMessage.warning('注册功能已关闭')
            return false
        }

        const valid = await registerFormRef.value.validate().catch(() => false)
        if (!valid) {
            ElMessage.error('请填写正确的注册信息')
            return false
        }

        loading.value = true
        try {
            const { data } = await api.auth.register(registerForm)
            if (data && data.code === 200) {
                ElMessage.success('注册成功')
                return true
            }
            ElMessage.error(data?.msg || '注册失败')
            return false
        } catch (error) {
            ElMessage.error(error?.response?.data?.msg || '注册失败')
            return false
        } finally {
            loading.value = false
        }
    }

    return {
        registerFormRef,
        registerForm,
        registerRules,
        loading,
        registerEnabled,
        codeLoading,
        codeCountdown,
        codeButtonText,
        sendEmailCode,
        submitRegister,
    }
}
