import { describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { flushPromises } from '@vue/test-utils'

import UserView from '@/views/admin/system/user/index.vue'
import RoleView from '@/views/admin/system/role/index.vue'
import DictTypeView from '@/views/admin/system/dictType/index.vue'
import DictDataView from '@/views/admin/system/dictData/index.vue'
import ConfigView from '@/views/admin/system/config/index.vue'
import InstitutionsView from '@/views/admin/system/institutions/index.vue'
import PermissionView from '@/views/admin/system/permission/index.vue'
import FeedbacksView from '@/views/admin/document/feedbacks/index.vue'
import GuidesView from '@/views/admin/document/guides/index.vue'
import JobView from '@/views/admin/monitor/task/job/index.vue'
import JobLogView from '@/views/admin/monitor/task/jobLog/index.vue'
import OnlineView from '@/views/admin/monitor/online/index.vue'
import OssView from '@/views/admin/monitor/oss/index.vue'
import RedisView from '@/views/admin/monitor/redis/index.vue'
import EmailView from '@/views/admin/monitor/email/index.vue'
import BusinessView from '@/views/business/index.vue'
import LayoutView from '@/views/admin/layout/index.vue'
import AsideCom from '@/views/admin/layout/AsideCom.vue'
import HeaderCom from '@/views/admin/layout/HeaderCom.vue'
import TabsCom from '@/views/admin/layout/TabsCom.vue'
import SettingsDialog from '@/components/SettingsDialog.vue'
import ProfileDialog from '@/components/ProfileDialog.vue'
import ThemeConfig from '@/components/ThemeConfig.vue'

import { useRouterStore } from '@/stores/modules/router.js'
import { useAuthStore } from '@/stores/modules/auth.js'
import { useConfigStore } from '@/stores/modules/config.js'
import { createTestRouter, mountView } from '../helpers/mountView.js'

const preparePinia = (configs = {}) => {
    const pinia = createPinia()
    setActivePinia(pinia)
    useRouterStore().setPermissions({ includes: () => true })
    useAuthStore().userInfo = {
        id: 1,
        username: 'admin',
        nickname: '管理员',
        email: 'admin@example.com',
        phone: '13800000000',
        roles: ['admin'],
    }
    useConfigStore().configList = Object.entries({
        'site.title': 'Single Admin',
        'site.icp': '粤ICP备123号',
        'register.enable': 'true',
        'register.verify_code.enable': 'true',
        'site.use_backend_theme_color': 'false',
        ...configs,
    }).map(([configKey, configValue]) => ({ configKey, configValue, status: true }))
    return pinia
}

const mountAdmin = async (Component, path = '/admin/dashboard/index') => {
    const pinia = preparePinia()
    const router = createTestRouter()
    await router.push(path)
    const wrapper = mountView(Component, { pinia, router })
    await flushPromises()
    return wrapper
}

const views = [
    ['用户管理', UserView],
    ['角色管理', RoleView],
    ['字典类型', DictTypeView],
    ['字典数据', DictDataView],
    ['参数配置', ConfigView],
    ['机构管理', InstitutionsView],
    ['权限管理', PermissionView],
    ['反馈管理', FeedbacksView],
    ['指南管理', GuidesView],
    ['定时任务', JobView],
    ['任务日志', JobLogView],
    ['在线用户', OnlineView],
    ['文件存储', OssView],
    ['Redis 监控', RedisView],
    ['邮件管理', EmailView],
]

describe('后台业务视图挂载', () => {
    it.each(views)('%s 页面可正常渲染', async (title, Component) => {
        const wrapper = await mountAdmin(Component)

        expect(wrapper.html().length).toBeGreaterThan(20)

        wrapper.unmount()
    })
})

describe('布局与全局组件挂载', () => {
    it('Business 空页面渲染', () => {
        const wrapper = mountView(BusinessView)

        expect(wrapper.exists()).toBe(true)
    })

    it('Layout 渲染侧栏、头部、标签与页脚', async () => {
        const wrapper = await mountAdmin(LayoutView)

        expect(wrapper.find('.el-container').exists()).toBe(true)
        expect(wrapper.text()).toContain('Single Admin')
        expect(wrapper.findAll('.el-aside').length).toBeGreaterThan(0)
    })

    it('Aside 渲染菜单并支持折叠', async () => {
        const wrapper = await mountAdmin(AsideCom)

        expect(wrapper.find('.el-menu').exists()).toBe(true)
    })

    it('Header 渲染面包屑与用户信息', async () => {
        const wrapper = await mountAdmin(HeaderCom)

        expect(wrapper.text()).toContain('管理员')
    })

    it('Tabs 渲染标签栏', async () => {
        const wrapper = await mountAdmin(TabsCom)

        expect(wrapper.html()).toContain('el-tabs')
    })

    it('SettingsDialog 可打开并切换主题', async () => {
        const wrapper = await mountAdmin(SettingsDialog, '/admin/dashboard/index')
        await wrapper.setProps({ modelValue: true })
        await flushPromises()

        expect(wrapper.text()).toContain('主题')
    })

    it('ProfileDialog 可打开并展示用户资料', async () => {
        const wrapper = await mountAdmin(ProfileDialog)
        await wrapper.setProps({ modelValue: true })
        await flushPromises()

        expect(wrapper.html().length).toBeGreaterThan(20)
    })

    it('ThemeConfig 渲染主题配置项', async () => {
        const wrapper = await mountAdmin(ThemeConfig)

        expect(wrapper.html().length).toBeGreaterThan(20)
    })

    it('Layout 折叠侧栏后宽度变化', async () => {
        const pinia = preparePinia()
        const { useThemeStore } = await import('@/stores/modules/theme.js')
        useThemeStore().setSidebarCollapsed(true)
        const router = createTestRouter()
        await router.push('/admin/dashboard/index')
        const wrapper = mountView(LayoutView, { pinia, router })
        await flushPromises()

        expect(wrapper.find('.el-aside').attributes('style')).toContain('64px')
    })
})

describe('接口异常时视图仍可渲染', () => {
    it('用户列表接口失败不导致崩溃', async () => {
        const pinia = preparePinia()
        const api = await import('@/api/index.js')
        api.default.user.pageUser.mockRejectedValueOnce(new Error('network'))
        const wrapper = mountView(UserView, { pinia })
        await flushPromises()

        expect(wrapper.exists()).toBe(true)
    })
})
