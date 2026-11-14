import { test, expect } from '@playwright/test'
import { mockApi } from './helpers/mockApi.js'

/**
 * E2E 主流程 1：登录
 * 覆盖表单校验、接口调用与登录后跳转到管理后台。
 */
test.describe('登录流程', () => {
    test('正确填写账号密码后进入管理后台', async ({ page }) => {
        await mockApi(page)
        await page.goto('/')

        await expect(page.getByPlaceholder('请输入用户名或邮箱')).toBeVisible()

        await page.getByPlaceholder('请输入用户名或邮箱').fill('admin')
        await page.getByPlaceholder('请输入密码').fill('123456')
        await page.getByRole('button', { name: '登 录' }).click()

        await expect(page).toHaveURL(/\/admin/)
        // 限定侧边栏菜单，避免面包屑 / Tab 中的同名文本触发 strict mode
        await expect(page.locator('.el-menu').getByText('首页', { exact: true })).toBeVisible()
    })

    test('未填写密码时保持登录页', async ({ page }) => {
        await mockApi(page)
        await page.goto('/')

        await page.getByPlaceholder('请输入用户名或邮箱').fill('admin')
        await page.getByPlaceholder('请输入密码').fill('')
        await page.getByRole('button', { name: '登 录' }).click()

        await expect(page.getByPlaceholder('请输入密码')).toBeVisible()
    })
})
