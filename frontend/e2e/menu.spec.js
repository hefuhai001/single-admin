import { test, expect } from '@playwright/test'
import { mockApi, DYNAMIC_ROUTES } from './helpers/mockApi.js'

/**
 * E2E 主流程 2：菜单渲染
 * 验证登录后侧边栏按后端返回的动态路由渲染层级菜单。
 */
test.describe('菜单渲染', () => {
    test('登录后侧边栏渲染后端返回的菜单', async ({ page }) => {
        await mockApi(page, { routes: DYNAMIC_ROUTES })
        await page.goto('/')

        await page.getByPlaceholder('请输入用户名或邮箱').fill('admin')
        await page.getByPlaceholder('请输入密码').fill('123456')
        await page.getByRole('button', { name: '登 录' }).click()

        await expect(page).toHaveURL(/\/admin/)

        // 只断言侧边栏内的菜单项，避免面包屑 / Tab 中的同名文本歧义
        const sidebar = page.locator('.el-menu')
        await expect(sidebar.getByText('首页', { exact: true })).toBeVisible()
        await expect(sidebar.getByText('系统管理', { exact: true })).toBeVisible()

        // 展开系统管理后可见子菜单
        await sidebar.getByText('系统管理', { exact: true }).click()
        await expect(sidebar.getByText('用户管理', { exact: true })).toBeVisible()
    })
})
