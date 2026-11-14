import { test, expect } from '@playwright/test'
import { mockApi, LIMITED_PERMISSIONS } from './helpers/mockApi.js'

/**
 * E2E 主流程 3：按钮级权限
 * 仅具备 user:query 权限时，查询表单可见，但新增/批量删除等按钮不渲染。
 */
test.describe('按钮级权限', () => {
    test('受限账号只能看到查询相关按钮', async ({ page }) => {
        await mockApi(page, { permissions: LIMITED_PERMISSIONS })
        await page.goto('/')

        await page.getByPlaceholder('请输入用户名或邮箱').fill('admin')
        await page.getByPlaceholder('请输入密码').fill('123456')
        await page.getByRole('button', { name: '登 录' }).click()
        await expect(page).toHaveURL(/\/admin/)

        await page.goto('/admin/system/user')

        // 具备 user:query：查询表单可见
        await expect(page.getByPlaceholder('请输入用户名')).toBeVisible()

        // 缺少 user:add / user:delete：对应按钮不渲染
        await expect(page.getByRole('button', { name: '新增' })).toHaveCount(0)
        await expect(page.getByRole('button', { name: '批量删除' })).toHaveCount(0)
    })
})
