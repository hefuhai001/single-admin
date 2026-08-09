// 主库
import { createApp } from 'vue'
// 全局状态管理
import pinia from '@/stores/index.js'
// 根组件
import App from './App.vue'
// 路由
import router from './router'
// 重置默认样式
import '@/assets/css/reset.css'
// tailwindcss
import '@/assets/css/tailwind.css'
// element主题色覆盖
import '@/assets/css/element.css'
// OverlayScrollbars 自定义滚动条
import 'overlayscrollbars/overlayscrollbars.css'
// 右键菜单
import ContextMenu from '@imengyu/vue3-context-menu'
import '@imengyu/vue3-context-menu/lib/vue3-context-menu.css'
// 实例化
const app = createApp(App)
// 动态渲染图标
import 'virtual:svg-icons-register'
// 组件库
import ElementPlus from 'element-plus'
// 组件库默认样式
import 'element-plus/dist/index.css'
// 必须引入暗黑主题的css变量文件
import 'element-plus/theme-chalk/dark/css-vars.css'
// 中文
import zhCn from 'element-plus/es/locale/lang/zh-cn'
// element图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
// 循环挂载
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}
// 自定义指令
import directives from './directives'
// 主题管理
import { useThemeStore } from '@/stores/modules/theme'
// 全局配置管理
import { useConfigStore } from '@/stores/modules/config'
// 全局状态管理
app.use(pinia)
// 初始化全局配置，完成后用数据库主题色覆盖本地默认主题色
const configStore = useConfigStore()
const themeStore = useThemeStore()
configStore
    .loadConfig()
    .then(() => {
        document.title = configStore.getValue('site.title', 'Single Admin')
        const useBackendColor = configStore.isEnabled('site.use_backend_theme_color')
        const primaryColor = useBackendColor ? configStore.getValue('site.primary_color') : null
        themeStore.initTheme(primaryColor)
    })
    .catch((error) => {
        console.error('加载全局配置失败:', error)
        themeStore.initTheme()
    })

app.use(router)
app.use(directives)
app.use(ElementPlus, { locale: zhCn })
app.use(ContextMenu)

app.mount('#app')
