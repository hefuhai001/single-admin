import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { flushPromises } from '@vue/test-utils'
import { ElMessageBox } from 'element-plus'

import UserView from '@/views/admin/system/user/index.vue'
import OnlineView from '@/views/admin/monitor/online/index.vue'
import { apiMock } from '../helpers/apiMock.js'
import { requestMock } from '../helpers/requestMock.js'
import { useRouterStore } from '@/stores/modules/router.js'
import { clickButton, findButton, mountView } from '../helpers/mountView.js'

const makePinia = (permissions = { includes: () => true }) => {
    const pinia = createPinia()
    setActivePinia(pinia)
    useRouterStore().setPermissions(permissions)
    return pinia
}

describe('用户管理交互', () => {
    beforeEach(() => {
        apiMock.user.pageUser.mockClear()
        apiMock.user.saveUser.mockClear()
        apiMock.user.updateUser.mockClear()
        apiMock.user.deleteUser.mockClear()
        apiMock.user.batchDeleteUser.mockClear()
        apiMock.user.getUserById.mockClear()
        vi.spyOn(ElMessageBox, 'confirm').mockResolvedValue()
    })

    it('刷新按钮重置查询条件并重新加载', async () => {
        const wrapper = mountView(UserView, { pinia: makePinia() })
        await flushPromises()
        expect(apiMock.user.pageUser).toHaveBeenCalledTimes(1)

        await clickButton(wrapper, '刷新')
        await flushPromises()

        expect(apiMock.user.pageUser).toHaveBeenCalledTimes(2)
        expect(wrapper.text()).toContain('admin')
    })

    it('新增用户填写表单后提交', async () => {
        const wrapper = mountView(UserView, { pinia: makePinia() })
        await flushPromises()

        await clickButton(wrapper, '新增')
        await flushPromises()
        expect(wrapper.text()).toContain('新增用户')

        const inputs = wrapper.findAll('.el-dialog input')
        await inputs[0].setValue('new-user')
        await inputs[1].setValue('123456')
        await inputs[2].setValue('新用户')
        await inputs[3].setValue('new@example.com')
        await inputs[4].setValue('13800000000')

        await clickButton(wrapper, '确定')
        await flushPromises()

        expect(apiMock.user.saveUser).toHaveBeenCalledWith(
            expect.objectContaining({ username: 'new-user', nickname: '新用户' }),
        )
    })

    it('编辑用户时加载详情并提交更新', async () => {
        const wrapper = mountView(UserView, { pinia: makePinia() })
        await flushPromises()

        const editButton = wrapper.findAll('button').find((button) => button.text().includes('编辑'))
        await editButton.trigger('click')
        await flushPromises()

        expect(apiMock.user.getUserById).toHaveBeenCalledWith(1)
        expect(wrapper.text()).toContain('编辑用户')

        await clickButton(wrapper, '确定')
        await flushPromises()

        expect(apiMock.user.updateUser).toHaveBeenCalled()
    })

    it('删除用户确认后调用删除接口', async () => {
        const wrapper = mountView(UserView, { pinia: makePinia() })
        await flushPromises()

        const deleteButton = wrapper
            .findAll('button')
            .find((button) => button.text().trim() === '删除')
        await deleteButton.trigger('click')
        await flushPromises()

        expect(ElMessageBox.confirm).toHaveBeenCalled()
        expect(apiMock.user.deleteUser).toHaveBeenCalledWith(1)
    })

    it('批量删除选中记录', async () => {
        const wrapper = mountView(UserView, { pinia: makePinia() })
        await flushPromises()

        wrapper.findComponent({ name: 'ElTable' }).vm.$emit('selection-change', [{ id: 1 }])
        await flushPromises()

        await clickButton(wrapper, '批量删除')
        await flushPromises()

        expect(apiMock.user.batchDeleteUser).toHaveBeenCalledWith([1])
    })

    it('取消删除时不提示失败且不调用接口', async () => {
        ElMessageBox.confirm.mockRejectedValueOnce('cancel')
        const wrapper = mountView(UserView, { pinia: makePinia() })
        await flushPromises()

        const deleteButton = wrapper
            .findAll('button')
            .find((button) => button.text().trim() === '删除')
        await deleteButton.trigger('click')
        await flushPromises()

        expect(apiMock.user.deleteUser).not.toHaveBeenCalled()
    })

    it('无权限时不渲染查询表单与新增按钮', async () => {
        const wrapper = mountView(UserView, {
            pinia: makePinia({ includes: () => false }),
        })
        await flushPromises()

        expect(findButton(wrapper, '新增')).toBeUndefined()
        expect(wrapper.find('form').exists()).toBe(false)
        expect(apiMock.user.pageUser).toHaveBeenCalled()
    })
})

describe('在线用户交互', () => {
    beforeEach(() => {
        requestMock.get.mockClear()
        requestMock.delete.mockClear()
        vi.spyOn(ElMessageBox, 'confirm').mockResolvedValue()
    })

    it('加载在线用户并可强制下线', async () => {
        const wrapper = mountView(OnlineView, { pinia: makePinia() })
        await flushPromises()

        expect(requestMock.get).toHaveBeenCalledWith('/admin/monitor/online')

        const kickButton = wrapper
            .findAll('button')
            .find((button) => button.text().includes('强退'))
        await kickButton.trigger('click')
        await flushPromises()

        expect(apiMock.online.kickOnlineUser).not.toHaveBeenCalled()
        expect(requestMock.delete).toHaveBeenCalledWith('/admin/monitor/online/token-1')
    })
})
