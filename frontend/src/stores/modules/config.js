import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import api from '@/api/index.js'
import { ElMessage } from 'element-plus'

export const useConfigStore = defineStore(
    'config',
    () => {
        // 原始配置列表
        const configList = ref([])
        // 加载状态
        const loading = ref(false)

        // 以 configKey 为键的配置映射，方便快速判断
        const configMap = computed(() => {
            const map = {}
            configList.value.forEach((item) => {
                if (item.configKey) {
                    map[item.configKey] = item
                }
            })
            return map
        })

        // 根据配置键获取原始配置对象
        function getConfig(key) {
            return configMap.value[key] || null
        }

        // 根据配置键获取配置值
        function getValue(key, defaultValue = '') {
            const config = configMap.value[key]
            if (!config || !config.status) {
                return defaultValue
            }
            return config.configValue || defaultValue
        }

        // 根据配置键判断布尔配置是否启用
        function isEnabled(key) {
            const config = configMap.value[key]
            if (!config || !config.status) {
                return false
            }
            const value = String(config.configValue).toLowerCase()
            return value === 'true' || value === '1' || value === 'yes' || value === 'on'
        }

        // 加载全部配置
        async function loadConfig() {
            loading.value = true
            try {
                const { data } = await api.config.listConfig()
                if (data && data.code === 200 && Array.isArray(data.data)) {
                    configList.value = data.data
                    return configList.value
                }
                return []
            } catch (error) {
                console.error('加载系统配置失败:', error)
                ElMessage.error('加载系统配置失败')
                throw error
            } finally {
                loading.value = false
            }
        }

        // 清空配置
        function clearConfig() {
            configList.value = []
        }

        return {
            configList,
            loading,
            configMap,
            getConfig,
            getValue,
            isEnabled,
            loadConfig,
            clearConfig,
        }
    },
    {
        persist: {
            key: 'config',
            paths: ['configList'],
        },
    },
)
