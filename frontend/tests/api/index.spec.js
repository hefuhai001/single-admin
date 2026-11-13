import { describe, expect, it, vi } from 'vitest'

vi.unmock('@/api/index.js')

import api from '@/api/index.js'

describe('api 聚合入口', () => {
    it('汇总所有业务模块', () => {
        expect(Object.keys(api)).toEqual([
            'auth',
            'permission',
            'role',
            'user',
            'feedbacks',
            'guides',
            'institutions',
            'dictType',
            'dictData',
            'config',
            'captcha',
            'email',
            'online',
            'oss',
            'job',
            'jobLog',
            'redis',
        ])
    })

    it('每个模块暴露可调用方法', () => {
        Object.values(api).forEach((module) => {
            expect(Object.keys(module).length).toBeGreaterThan(0)
            Object.values(module).forEach((fn) => expect(typeof fn).toBe('function'))
        })
    })
})
