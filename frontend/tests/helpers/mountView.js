import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import ElementPlus from 'element-plus'
import copy from '@/directives/modules/copy.js'
import debounce from '@/directives/modules/debounce.js'
import throttle from '@/directives/modules/throttle.js'

const Empty = { template: '<div class="route-view"><router-view /></div>' }

export function createTestRouter() {
    const router = createRouter({
        history: createMemoryHistory(),
        routes: [
            { path: '/', name: 'login', component: Empty, meta: { title: '登录' } },
            { path: '/register', name: 'register', component: Empty, meta: { title: '注册' } },
            {
                path: '/admin',
                name: 'admin',
                component: Empty,
                redirect: '/admin/dashboard/index',
                meta: { title: '仪表盘' },
                children: [
                    {
                        path: 'dashboard/index',
                        name: 'Dashboard',
                        component: Empty,
                        meta: { title: '仪表盘', affix: true },
                    },
                    { path: 'user', name: 'User', component: Empty, meta: { title: '用户管理' } },
                ],
            },
            { path: '/:pathMatch(.*)*', name: 'NotFound', component: Empty, meta: { title: '404' } },
        ],
    })
    return router
}

export function mountView(Component, options = {}) {
    const pinia = options.pinia || createPinia()
    setActivePinia(pinia)

    const router = options.router || createTestRouter()

    return mount(Component, {
        props: options.props,
        attachTo: options.attachTo,
        global: {
            plugins: [pinia, router, ElementPlus],
            directives: { copy, debounce, throttle },
            stubs: {
                teleport: true,
                OverlayScrollbarsComponent: {
                    template: '<div class="overlay-scrollbars"><slot /></div>',
                },
                ElSelect: {
                    props: ['modelValue'],
                    emits: ['update:modelValue', 'change'],
                    template: '<div class="el-select"><slot /></div>',
                },
                ElOption: {
                    props: ['label', 'value'],
                    template: '<div class="el-option">{{ label }}<slot /></div>',
                },
                ElDropdown: {
                    template: '<div class="el-dropdown"><slot /><slot name="dropdown" /></div>',
                },
                ElDropdownMenu: {
                    template: '<div class="el-dropdown-menu"><slot /></div>',
                },
                ElDropdownItem: {
                    props: ['command'],
                    template: '<div class="el-dropdown-item"><slot /></div>',
                },
                transition: false,
                'transition-group': false,
                ...options.stubs,
            },
        },
    })
}

export function findButton(wrapper, text) {
    return wrapper.findAll('button').find((button) => button.text().includes(text))
}

export async function clickButton(wrapper, text) {
    const button = findButton(wrapper, text)
    if (!button) return false
    await button.trigger('click')
    return true
}

export async function settle(wrapper) {
    await flushPromises()
    await wrapper.vm.$nextTick()
    await flushPromises()
}
