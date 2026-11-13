import { describe, expect, it } from 'vitest'

import config from '@/config/index.js'
import pinia, { default as storesPinia } from '@/stores/index.js'

describe('全局配置与 Pinia 实例', () => {
    it('config 默认导出空配置对象', () => {
        expect(config).toEqual({})
    })

    it('stores 默认导出已安装持久化插件的 Pinia 实例', () => {
        expect(pinia).toBe(storesPinia)
        expect(typeof pinia.install).toBe('function')
        expect(pinia._p).toBeTruthy()
    })
})
