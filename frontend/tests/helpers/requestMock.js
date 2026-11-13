import { vi } from 'vitest'

export const row = {
    id: 1,
    username: 'admin',
    nickname: '管理员',
    email: 'admin@example.com',
    phone: '13800000000',
    institutionId: 1,
    status: 1,
    roleName: '管理员',
    roleCode: 'admin',
    dictName: '性别',
    dictType: 'sex',
    configKey: 'site.title',
    configValue: 'Single Admin',
    permissionName: '用户查询',
    permissionCode: 'user:query',
    jobName: '示例任务',
    jobGroup: 'DEFAULT',
    invokeTarget: 'demo.run',
    cronExpression: '0 0 * * * ?',
    userId: 1,
    deviceId: 'device-1',
    loginIp: '127.0.0.1',
    loginTime: '2026-01-01 00:00:00',
    token: 'token-1',
    bucketName: 'demo',
    objectName: 'a.txt',
    key: 'demo:key',
    createdAt: '2026-01-01 00:00:00',
    updatedAt: '2026-01-01 00:00:00',
}

const response = (data) => ({ data: { code: 200, msg: 'ok', data } })

const resolveData = (config) => {
    const method = (config?.method || 'get').toLowerCase()
    const url = typeof config === 'string' ? config : config?.url || ''

    if (method !== 'get') {
        return true
    }
    if (url.includes('/page')) {
        return { records: [row], total: 1 }
    }
    if (url.includes('/list') || url.includes('/tree') || url.includes('/routes')) {
        return [row]
    }
    if (url.includes('/permissions')) {
        return []
    }
    if (url.includes('/online')) {
        return [row]
    }
    if (url.includes('/buckets')) {
        return ['demo']
    }
    return row
}

const handler = (config) => Promise.resolve(response(resolveData(config)))

export const requestMock = {
    get: vi.fn(handler),
    post: vi.fn(handler),
    put: vi.fn(handler),
    delete: vi.fn(handler),
}
