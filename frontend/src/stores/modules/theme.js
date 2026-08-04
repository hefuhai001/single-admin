import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

const defaultTheme = {
    mode: 'light',
    primaryColor: '#409EFF',
    themeKey: 'blue',
    fontSize: 14,
    sidebarCollapsed: false,
    showBreadcrumb: true,
    showFooter: true,
}

const themePresets = {
    blue: {
        key: 'blue',
        label: '科技蓝',
        icon: '🔵',
        description: '专业、信任',
        primaryColor: '#409EFF',
        preview: 'linear-gradient(135deg, #3b82f6, #1d4ed8)',
    },
    violet: {
        key: 'violet',
        label: '紫罗兰',
        icon: '🟣',
        description: '创意、优雅',
        primaryColor: '#8b5cf6',
        preview: 'linear-gradient(135deg, #8b5cf6, #6d28d9)',
    },
    orange: {
        key: 'orange',
        label: '活力橙',
        icon: '🟠',
        description: '热情、活力',
        primaryColor: '#f97316',
        preview: 'linear-gradient(135deg, #f97316, #c2410c)',
    },
    rose: {
        key: 'rose',
        label: '玫瑰红',
        icon: '🔴',
        description: '浪漫、醒目',
        primaryColor: '#f43f5e',
        preview: 'linear-gradient(135deg, #f43f5e, #be123c)',
    },
    cyan: {
        key: 'cyan',
        label: '清新青',
        icon: '🔷',
        description: '清新、科技',
        primaryColor: '#06b6d4',
        preview: 'linear-gradient(135deg, #06b6d4, #0e7490)',
    },
}

export const useThemeStore = defineStore(
    'theme',
    () => {
        const theme = ref({ ...defaultTheme })

        const getMode = computed(() => theme.value.mode)
        const getPrimaryColor = computed(() => theme.value.primaryColor)
        const getThemeKey = computed(() => theme.value.themeKey)
        const getFontSize = computed(() => theme.value.fontSize)
        const getSidebarCollapsed = computed(() => theme.value.sidebarCollapsed)
        const getShowBreadcrumb = computed(() => theme.value.showBreadcrumb)
        const getShowFooter = computed(() => theme.value.showFooter)
        const isDark = computed(() => theme.value.mode === 'dark')

        const currentTheme = computed(() => themePresets[theme.value.themeKey] || themePresets.blue)

        function setMode(mode) {
            theme.value.mode = mode
            applyTheme()
        }

        function toggleMode() {
            theme.value.mode = theme.value.mode === 'light' ? 'dark' : 'light'
            applyTheme()
        }

        function setPrimaryColor(color) {
            theme.value.primaryColor = color
            applyPrimaryColor()
        }

        function setThemeKey(key) {
            const preset = themePresets[key]
            if (preset) {
                theme.value.themeKey = key
                theme.value.primaryColor = preset.primaryColor
                applyThemeKey()
            }
        }

        function applyThemeKey() {
            const html = document.documentElement
            html.setAttribute('data-theme', theme.value.themeKey)
            applyPrimaryColor()
        }

        function setFontSize(size) {
            theme.value.fontSize = size
            document.documentElement.style.fontSize = `${size}px`
        }

        function toggleSidebar() {
            theme.value.sidebarCollapsed = !theme.value.sidebarCollapsed
        }

        function setSidebarCollapsed(collapsed) {
            theme.value.sidebarCollapsed = collapsed
        }

        function setShowBreadcrumb(show) {
            theme.value.showBreadcrumb = show
        }

        function setShowFooter(show) {
            theme.value.showFooter = show
        }

        function setTheme(newTheme) {
            theme.value = { ...defaultTheme, ...newTheme }
            applyTheme()
        }

        function resetTheme() {
            theme.value = { ...defaultTheme }
            applyTheme()
        }

        function applyTheme() {
            const html = document.documentElement
            if (theme.value.mode === 'dark') {
                html.classList.add('dark')
            } else {
                html.classList.remove('dark')
            }
            applyThemeKey()
        }

        function applyPrimaryColor() {
            const primaryColor = theme.value.primaryColor
            const el = document.documentElement.style

            el.setProperty('--el-color-primary', primaryColor)
            el.setProperty('--el-color-primary-light-3', mixColor(primaryColor, '#ffffff', 0.3))
            el.setProperty('--el-color-primary-light-5', mixColor(primaryColor, '#ffffff', 0.5))
            el.setProperty('--el-color-primary-light-7', mixColor(primaryColor, '#ffffff', 0.7))
            el.setProperty('--el-color-primary-light-8', mixColor(primaryColor, '#ffffff', 0.8))
            el.setProperty('--el-color-primary-light-9', mixColor(primaryColor, '#ffffff', 0.9))
            el.setProperty('--el-color-primary-dark-2', mixColor(primaryColor, '#000000', 0.2))
        }

        function mixColor(color1, color2, ratio) {
            const hex = (c) => {
                const hex = c.replace('#', '')
                return parseInt(hex, 16)
            }

            const r1 = (hex(color1) >> 16) & 0xff
            const g1 = (hex(color1) >> 8) & 0xff
            const b1 = hex(color1) & 0xff

            const r2 = (hex(color2) >> 16) & 0xff
            const g2 = (hex(color2) >> 8) & 0xff
            const b2 = hex(color2) & 0xff

            const r = Math.round(r1 * (1 - ratio) + r2 * ratio)
            const g = Math.round(g1 * (1 - ratio) + g2 * ratio)
            const b = Math.round(b1 * (1 - ratio) + b2 * ratio)

            return `#${((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1)}`
        }

        function initTheme() {
            applyTheme()
            setFontSize(theme.value.fontSize)
        }

        function getThemePresets() {
            return Object.values(themePresets)
        }

        return {
            theme,
            getMode,
            getPrimaryColor,
            getThemeKey,
            getFontSize,
            getSidebarCollapsed,
            getShowBreadcrumb,
            getShowFooter,
            isDark,
            currentTheme,
            setMode,
            toggleMode,
            setPrimaryColor,
            setThemeKey,
            setFontSize,
            toggleSidebar,
            setSidebarCollapsed,
            setShowBreadcrumb,
            setShowFooter,
            setTheme,
            resetTheme,
            initTheme,
            getThemePresets,
        }
    },
    {
        persist: {
            key: 'theme',
            storage: localStorage,
        },
    },
)
