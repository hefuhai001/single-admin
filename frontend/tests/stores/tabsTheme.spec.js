import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

const { routerPush } = vi.hoisted(() => ({ routerPush: vi.fn() }))

vi.mock('vue-router', () => ({ useRouter: () => ({ push: routerPush }) }))

import { useTabsStore } from '@/stores/modules/tabs.js'
import { useThemeStore } from '@/stores/modules/theme.js'

const makeRoute = (path, name, meta = {}, query) => ({ path, name, meta, query })

describe('tabs store', () => {
    beforeEach(() => {
        setActivePinia(createPinia())
        routerPush.mockClear()
    })

    it('addTab 忽略没有 name 的路由', () => {
        const store = useTabsStore()

        store.addTab(makeRoute('/no-name', undefined))

        expect(store.visitedTabs).toEqual([])
    })

    it('addTab 新增标签并拼接 query 为 fullPath', () => {
        const store = useTabsStore()

        store.addTab(makeRoute('/admin/user', 'User', { title: '用户管理' }, { page: 2 }))

        expect(store.visitedTabs).toHaveLength(1)
        expect(store.visitedTabs[0]).toEqual(
            expect.objectContaining({
                title: '用户管理',
                fullPath: '/admin/user?page=2',
                affix: false,
                noCache: false,
            }),
        )
        expect(store.activeTab).toBe('/admin/user?page=2')
    })

    it('addTab 标题缺省时使用路由 name', () => {
        const store = useTabsStore()

        store.addTab(makeRoute('/admin/role', 'Role'))

        expect(store.visitedTabs[0].title).toBe('Role')
    })

    it('addTab 已存在 path 时只更新 query 与 fullPath', () => {
        const store = useTabsStore()
        store.addTab(makeRoute('/admin/user', 'User', { title: '用户' }, { page: 1 }))

        store.addTab(makeRoute('/admin/user', 'User', { title: '用户' }, { page: 2 }))

        expect(store.visitedTabs).toHaveLength(1)
        expect(store.visitedTabs[0].fullPath).toBe('/admin/user?page=2')
    })

    it('cachedTabs 过滤 noCache 标签', () => {
        const store = useTabsStore()
        store.addTab(makeRoute('/a', 'A'))
        store.addTab(makeRoute('/b', 'B', { noCache: true }))

        expect(store.cachedTabs).toEqual(['A'])
    })

    it('closeTab 关闭非激活标签', () => {
        const store = useTabsStore()
        store.addTab(makeRoute('/a', 'A'))
        store.addTab(makeRoute('/b', 'B'))

        store.closeTab('/a')

        expect(store.visitedTabs.map((tab) => tab.name)).toEqual(['B'])
        expect(store.activeTab).toBe('/b')
    })

    it('closeTab 关闭激活标签后跳到下一个标签', () => {
        const store = useTabsStore()
        store.addTab(makeRoute('/a', 'A'))
        store.addTab(makeRoute('/b', 'B'))

        store.closeTab('/b')

        expect(store.activeTab).toBe('/a')
        expect(routerPush).toHaveBeenCalledWith({ path: '/a', query: {} })
    })

    it('closeTab 关闭最后一个标签后跳到前一个', () => {
        const store = useTabsStore()
        store.addTab(makeRoute('/a', 'A'))
        store.addTab(makeRoute('/b', 'B'))
        store.addTab(makeRoute('/c', 'C'))

        store.closeTab('/c')

        expect(store.activeTab).toBe('/b')
    })

    it('closeTab 对不存在的标签或固定标签不生效', () => {
        const store = useTabsStore()
        store.addTab(makeRoute('/fixed', 'Fixed', { affix: true }))
        store.addTab(makeRoute('/a', 'A'))

        store.closeTab('/missing')
        store.closeTab('/fixed')

        expect(store.visitedTabs).toHaveLength(2)
    })

    it('closeOtherTabs 保留固定标签与当前标签', () => {
        const store = useTabsStore()
        store.addTab(makeRoute('/fixed', 'Fixed', { affix: true }))
        store.addTab(makeRoute('/a', 'A'))
        store.addTab(makeRoute('/b', 'B'))

        store.closeOtherTabs('/b')

        expect(store.visitedTabs.map((tab) => tab.name)).toEqual(['Fixed', 'B'])
    })

    it('closeOtherTabs 当前激活标签被移除时切换到目标标签', () => {
        const store = useTabsStore()
        store.addTab(makeRoute('/a', 'A'))
        store.addTab(makeRoute('/b', 'B'))
        store.addTab(makeRoute('/c', 'C'))
        store.activeTab = '/c'

        store.closeOtherTabs('/a')

        expect(store.activeTab).toBe('/a')
        expect(routerPush).toHaveBeenCalledWith({ path: '/a', query: {} })
    })

    it('closeLeftTabs 关闭左侧标签并保留固定标签', () => {
        const store = useTabsStore()
        store.addTab(makeRoute('/fixed', 'Fixed', { affix: true }))
        store.addTab(makeRoute('/a', 'A'))
        store.addTab(makeRoute('/b', 'B'))

        store.closeLeftTabs('/b')

        expect(store.visitedTabs.map((tab) => tab.name)).toEqual(['Fixed', 'B'])
    })

    it('closeLeftTabs 对不存在的路径不生效', () => {
        const store = useTabsStore()
        store.addTab(makeRoute('/a', 'A'))

        store.closeLeftTabs('/missing')

        expect(store.visitedTabs).toHaveLength(1)
    })

    it('closeRightTabs 关闭右侧标签', () => {
        const store = useTabsStore()
        store.addTab(makeRoute('/a', 'A'))
        store.addTab(makeRoute('/b', 'B'))
        store.addTab(makeRoute('/c', 'C'))

        store.closeRightTabs('/b')

        expect(store.visitedTabs.map((tab) => tab.name)).toEqual(['A', 'B'])
    })

    it('closeAllTabs 只保留固定标签并激活最后一个', () => {
        const store = useTabsStore()
        store.addTab(makeRoute('/fixed', 'Fixed', { affix: true }))
        store.addTab(makeRoute('/a', 'A'))

        store.closeAllTabs()

        expect(store.visitedTabs.map((tab) => tab.name)).toEqual(['Fixed'])
        expect(store.activeTab).toBe('/fixed')
        expect(routerPush).toHaveBeenCalledWith({ path: '/fixed', query: {} })
    })

    it('closeAllTabs 无固定标签时清空', () => {
        const store = useTabsStore()
        store.addTab(makeRoute('/a', 'A'))

        store.closeAllTabs()

        expect(store.visitedTabs).toEqual([])
    })

    it('setActiveTab 更新激活项并跳转', () => {
        const store = useTabsStore()
        store.addTab(makeRoute('/a', 'A'))

        store.setActiveTab('/a')

        expect(store.activeTab).toBe('/a')
        expect(routerPush).toHaveBeenCalledWith({ path: '/a', query: {} })
    })

    it('setActiveTab 对不存在的标签只更新激活值', () => {
        const store = useTabsStore()

        store.setActiveTab('/missing')

        expect(store.activeTab).toBe('/missing')
        expect(routerPush).not.toHaveBeenCalled()
    })

    it('initTabs 只添加固定标签', () => {
        const store = useTabsStore()

        store.initTabs([
            makeRoute('/fixed', 'Fixed', { affix: true }),
            makeRoute('/normal', 'Normal'),
        ])

        expect(store.visitedTabs.map((tab) => tab.name)).toEqual(['Fixed'])
    })
})

describe('theme store', () => {
    beforeEach(() => {
        setActivePinia(createPinia())
        document.documentElement.className = ''
        document.documentElement.removeAttribute('data-theme')
        document.documentElement.style.cssText = ''
    })

    it('默认主题值正确', () => {
        const store = useThemeStore()

        expect(store.getMode).toBe('light')
        expect(store.getPrimaryColor).toBe('#409EFF')
        expect(store.getFontSize).toBe(14)
        expect(store.isDark).toBe(false)
        expect(store.currentTheme).toEqual({ label: '自定义主题', primaryColor: '#409EFF' })
        expect(store.getShowBreadcrumb).toBe(true)
        expect(store.getShowFooter).toBe(true)
        expect(store.getSidebarCollapsed).toBe(false)
    })

    it('setMode/toggleMode 切换暗色并更新 DOM', () => {
        const store = useThemeStore()

        store.setMode('dark')
        expect(store.getMode).toBe('dark')
        expect(store.isDark).toBe(true)
        expect(document.documentElement.classList.contains('dark')).toBe(true)

        store.toggleMode()
        expect(store.getMode).toBe('light')
        expect(document.documentElement.classList.contains('dark')).toBe(false)

        store.toggleMode()
        expect(store.getMode).toBe('dark')
    })

    it('setPrimaryColor 校验颜色并写入 CSS 变量', () => {
        const store = useThemeStore()

        store.setPrimaryColor('#ff0000')
        expect(store.getPrimaryColor).toBe('#ff0000')
        expect(document.documentElement.style.getPropertyValue('--el-color-primary')).toBe('#ff0000')
        expect(document.documentElement.style.getPropertyValue('--logo-primary')).toBe('#ff0000')

        store.setPrimaryColor('invalid')
        expect(store.getPrimaryColor).toBe('#409EFF')
    })

    it('setFontSize 更新根字号与 Element Plus 变量', () => {
        const store = useThemeStore()

        store.setFontSize(16)

        expect(store.getFontSize).toBe(16)
        expect(document.documentElement.style.fontSize).toBe('16px')
        expect(document.documentElement.style.getPropertyValue('--el-font-size-base')).toBe('16px')
        expect(
            document.documentElement.style.getPropertyValue('--el-font-size-extra-extra-extra-large'),
        ).toBe('37px')
    })

    it('侧边栏与布局开关可切换', () => {
        const store = useThemeStore()

        store.toggleSidebar()
        expect(store.getSidebarCollapsed).toBe(true)
        store.setSidebarCollapsed(false)
        expect(store.getSidebarCollapsed).toBe(false)

        store.setShowBreadcrumb(false)
        expect(store.getShowBreadcrumb).toBe(false)
        store.setShowFooter(false)
        expect(store.getShowFooter).toBe(false)
    })

    it('setTheme 合并默认值并校验主色', () => {
        const store = useThemeStore()

        store.setTheme({ mode: 'dark', primaryColor: 'bad', fontSize: 18 })

        expect(store.theme.mode).toBe('dark')
        expect(store.theme.primaryColor).toBe('#409EFF')
        expect(store.theme.fontSize).toBe(18)
        expect(store.theme.showFooter).toBe(true)
        expect(document.documentElement.classList.contains('dark')).toBe(true)
    })

    it('resetTheme 恢复默认配置', () => {
        const store = useThemeStore()
        store.setTheme({ mode: 'dark', primaryColor: '#000000', fontSize: 20 })

        store.resetTheme()

        expect(store.theme).toEqual(
            expect.objectContaining({ mode: 'light', primaryColor: '#409EFF', fontSize: 14 }),
        )
    })

    it('initTheme 支持外部合法主色并忽略非法值', () => {
        const store = useThemeStore()

        store.initTheme('#123456')
        expect(store.getPrimaryColor).toBe('#123456')

        store.initTheme('invalid')
        expect(store.getPrimaryColor).toBe('#123456')

        store.initTheme()
        expect(store.getPrimaryColor).toBe('#123456')
    })
})
