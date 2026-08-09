import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

const defaultTheme = {
    mode: 'light',
    primaryColor: '#409EFF',
    fontSize: 14,
    sidebarCollapsed: false,
    showBreadcrumb: true,
    showFooter: true,
}

export const useThemeStore = defineStore(
    'theme',
    () => {
        const theme = ref({ ...defaultTheme })

        const getMode = computed(() => theme.value.mode)
        const getPrimaryColor = computed(() => theme.value.primaryColor || defaultTheme.primaryColor)
        const getFontSize = computed(() => theme.value.fontSize)
        const getSidebarCollapsed = computed(() => theme.value.sidebarCollapsed)
        const getShowBreadcrumb = computed(() => theme.value.showBreadcrumb)
        const getShowFooter = computed(() => theme.value.showFooter)
        const isDark = computed(() => theme.value.mode === 'dark')

        const currentTheme = computed(() => ({
            label: '自定义主题',
            primaryColor: getPrimaryColor.value,
        }))

        function setMode(mode) {
            theme.value.mode = mode
            applyTheme()
        }

        function toggleMode() {
            theme.value.mode = theme.value.mode === 'light' ? 'dark' : 'light'
            applyTheme()
        }

        function setPrimaryColor(color) {
            theme.value.primaryColor = normalizeColor(color)
            applyPrimaryColor()
        }

        function applyThemeKey() {
            const html = document.documentElement
            html.setAttribute('data-theme', 'custom')
            applyPrimaryColor()
        }

        function normalizeColor(color) {
            return color && /^#([0-9A-Fa-f]{6})$/.test(color) ? color : defaultTheme.primaryColor
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
            theme.value.primaryColor = normalizeColor(theme.value.primaryColor)
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
            const primaryColor = normalizeColor(theme.value.primaryColor)
            theme.value.primaryColor = primaryColor
            const el = document.documentElement.style

            el.setProperty('--el-color-primary', primaryColor)
            el.setProperty('--el-color-primary-light-3', mixColor(primaryColor, '#ffffff', 0.3))
            el.setProperty('--el-color-primary-light-5', mixColor(primaryColor, '#ffffff', 0.5))
            el.setProperty('--el-color-primary-light-7', mixColor(primaryColor, '#ffffff', 0.7))
            el.setProperty('--el-color-primary-light-8', mixColor(primaryColor, '#ffffff', 0.8))
            el.setProperty('--el-color-primary-light-9', mixColor(primaryColor, '#ffffff', 0.9))
            el.setProperty('--el-color-primary-dark-2', mixColor(primaryColor, '#000000', 0.2))

            applyThemeScale(primaryColor)
        }

        function applyThemeScale(color) {
            const hsl = hexToHsl(color)
            const scale = generateThemeScale(hsl)
            const el = document.documentElement.style
            for (const [key, value] of Object.entries(scale)) {
                el.setProperty(key, value)
            }
            el.setProperty('--logo-primary', color)
            el.setProperty('--logo-light', scale['--theme-300'])
            el.setProperty('--logo-lighter', scale['--theme-100'])
        }

        function generateThemeScale(hsl) {
            const steps = {
                50: 97,
                100: 93,
                200: 86,
                300: 78,
                400: 68,
                500: 50,
                600: 42,
                700: 34,
                800: 26,
                900: 18,
                950: 10,
            }
            const scale = {}
            for (const [step, lightness] of Object.entries(steps)) {
                scale[`--theme-${step}`] = hslToCss(hsl.h, hsl.s, lightness)
            }
            return scale
        }

        function hexToHsl(hex) {
            let r = parseInt(hex.slice(1, 3), 16) / 255
            let g = parseInt(hex.slice(3, 5), 16) / 255
            let b = parseInt(hex.slice(5, 7), 16) / 255

            const max = Math.max(r, g, b)
            const min = Math.min(r, g, b)
            let h = 0
            let s = 0
            let l = (max + min) / 2

            if (max !== min) {
                const d = max - min
                s = l > 0.5 ? d / (2 - max - min) : d / (max + min)
                switch (max) {
                    case r:
                        h = (g - b) / d + (g < b ? 6 : 0)
                        break
                    case g:
                        h = (b - r) / d + 2
                        break
                    case b:
                        h = (r - g) / d + 4
                        break
                }
                h /= 6
            }

            return { h: h * 360, s: s * 100, l: l * 100 }
        }

        function hslToCss(h, s, l) {
            return `hsl(${Math.round(h)} ${Math.round(s)}% ${Math.round(l)}%)`
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

        return {
            theme,
            getMode,
            getPrimaryColor,
            getFontSize,
            getSidebarCollapsed,
            getShowBreadcrumb,
            getShowFooter,
            isDark,
            currentTheme,
            setMode,
            toggleMode,
            setPrimaryColor,
            setFontSize,
            toggleSidebar,
            setSidebarCollapsed,
            setShowBreadcrumb,
            setShowFooter,
            setTheme,
            resetTheme,
            initTheme,
        }
    },
    {
        persist: {
            key: 'theme',
            storage: localStorage,
        },
    },
)
