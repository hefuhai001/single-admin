import { vi } from 'vitest'

export const ok = (data) =>
    Promise.resolve({
        data: {
            code: 200,
            msg: 'ok',
            data,
        },
    })

export const sampleRow = {
    id: 1,
    username: 'admin',
    nickname: '管理员',
    email: 'admin@example.com',
    phone: '13800000000',
    institutionId: 1,
    status: 1,
    roleName: '管理员',
    roleCode: 'admin',
    description: '系统管理员',
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

const pageResult = () => ok({ records: [sampleRow], total: 1 })
const listResult = () => ok([sampleRow])
const detailResult = () => ok({ ...sampleRow })
const actionResult = () => ok(true)

const crud = (page, list, detail, save, update, remove, batch) => ({
    [page]: vi.fn(pageResult),
    [list]: vi.fn(listResult),
    [detail]: vi.fn(detailResult),
    [save]: vi.fn(actionResult),
    [update]: vi.fn(actionResult),
    [remove]: vi.fn(actionResult),
    [batch]: vi.fn(actionResult),
})

export const apiMock = {
    auth: {
        login: vi.fn(() => ok({ accessToken: 'access-token', refreshToken: 'refresh-token' })),
        refresh: vi.fn(() => ok({ accessToken: 'a', refreshToken: 'r' })),
        logout: vi.fn(actionResult),
        isLogin: vi.fn(() => ok(true)),
        emailLogin: vi.fn(actionResult),
        emailCode: vi.fn(actionResult),
        profile: vi.fn(() =>
            ok({ id: 1, username: 'admin', nickname: '管理员', roles: ['admin'] }),
        ),
        register: vi.fn(() => ok({ accessToken: 'access-token', refreshToken: 'refresh-token' })),
    },
    captcha: {
        getSliderCaptcha: vi.fn(() =>
            ok({
                token: 'captcha-token',
                backgroundImage: 'bg.png',
                sliderImage: 'slider.png',
                sliderY: 10,
            }),
        ),
        verifySlider: vi.fn(() => ok('captcha-token-ok')),
        sendEmailCode: vi.fn(actionResult),
    },
    email: {
        sendSimpleEmail: vi.fn(actionResult),
        sendHtmlEmail: vi.fn(actionResult),
        sendTemplateEmail: vi.fn(actionResult),
        sendBatchEmails: vi.fn(actionResult),
    },
    feedbacks: {
        ...crud(
            'pageFeedbacks',
            'listFeedbacks',
            'getFeedbacksById',
            'saveFeedbacks',
            'updateFeedbacks',
            'deleteFeedbacks',
            'batchDeleteFeedbacks',
        ),
        pageFeedbacks: vi.fn(pageResult),
        listFeedbacks: vi.fn(listResult),
        getFeedbacksById: vi.fn(detailResult),
        saveFeedbacks: vi.fn(actionResult),
        updateFeedbacks: vi.fn(actionResult),
        deleteFeedbacks: vi.fn(actionResult),
        batchDeleteFeedbacks: vi.fn(actionResult),
    },
    guides: {
        pageGuides: vi.fn(pageResult),
        listGuides: vi.fn(listResult),
        getGuidesById: vi.fn(detailResult),
        saveGuides: vi.fn(actionResult),
        updateGuides: vi.fn(actionResult),
        deleteGuides: vi.fn(actionResult),
        batchDeleteGuides: vi.fn(actionResult),
    },
    institutions: {
        treeInstitutions: vi.fn(() => ok([{ id: 1, institutionName: '总部', children: [] }])),
        pageInstitutions: vi.fn(pageResult),
        listInstitutions: vi.fn(listResult),
        getInstitutionsById: vi.fn(detailResult),
        saveInstitutions: vi.fn(actionResult),
        updateInstitutions: vi.fn(actionResult),
        deleteInstitutions: vi.fn(actionResult),
        batchDeleteInstitutions: vi.fn(actionResult),
    },
    permission: {
        pagePermission: vi.fn(pageResult),
        getPermissionById: vi.fn(detailResult),
        savePermission: vi.fn(actionResult),
        updatePermission: vi.fn(actionResult),
        deletePermission: vi.fn(actionResult),
        batchDeletePermission: vi.fn(actionResult),
        treePermission: vi.fn(() =>
            ok([{ id: 1, permissionName: '系统管理', children: [{ id: 2, permissionName: '用户' }] }]),
        ),
    },
    role: {
        pageRole: vi.fn(pageResult),
        list: vi.fn(() => ok([{ id: 1, roleName: '管理员' }])),
        getRoleById: vi.fn(() => ok({ ...sampleRow, permissions: [{ id: 1, children: [{ id: 2 }] }] })),
        saveRole: vi.fn(actionResult),
        updateRole: vi.fn(actionResult),
        deleteRole: vi.fn(actionResult),
        batchDeleteRole: vi.fn(actionResult),
    },
    user: {
        pageUser: vi.fn(pageResult),
        getUserById: vi.fn(detailResult),
        saveUser: vi.fn(actionResult),
        updateUser: vi.fn(actionResult),
        deleteUser: vi.fn(actionResult),
        batchDeleteUser: vi.fn(actionResult),
        routesUser: vi.fn(() => ok([])),
        permissionsUser: vi.fn(() => ok([])),
    },
    dictType: {
        pageDictType: vi.fn(pageResult),
        listDictType: vi.fn(listResult),
        getDictTypeById: vi.fn(detailResult),
        saveDictType: vi.fn(actionResult),
        updateDictType: vi.fn(actionResult),
        deleteDictType: vi.fn(actionResult),
        batchDeleteDictType: vi.fn(actionResult),
    },
    dictData: {
        pageDictData: vi.fn(pageResult),
        listDictData: vi.fn(listResult),
        getDictDataById: vi.fn(detailResult),
        saveDictData: vi.fn(actionResult),
        updateDictData: vi.fn(actionResult),
        deleteDictData: vi.fn(actionResult),
        batchDeleteDictData: vi.fn(actionResult),
    },
    config: {
        pageConfig: vi.fn(pageResult),
        listConfig: vi.fn(() =>
            ok([
                { configKey: 'site.title', configValue: 'Single Admin', status: true },
                { configKey: 'site.icp', configValue: '粤ICP备00000000号', status: true },
                { configKey: 'register.enable', configValue: 'true', status: true },
                { configKey: 'register.verify_code.enable', configValue: 'true', status: true },
                { configKey: 'site.use_backend_theme_color', configValue: 'false', status: true },
            ]),
        ),
        getConfigById: vi.fn(detailResult),
        saveConfig: vi.fn(actionResult),
        updateConfig: vi.fn(actionResult),
        deleteConfig: vi.fn(actionResult),
        batchDeleteConfig: vi.fn(actionResult),
    },
    online: {
        getOnlineUsers: vi.fn(() => ok([sampleRow])),
        kickOnlineUser: vi.fn(actionResult),
    },
    oss: {
        listBuckets: vi.fn(() => ok(['demo'])),
        createBucket: vi.fn(actionResult),
        deleteBucket: vi.fn(actionResult),
        listObjects: vi.fn(() => ok({ records: [sampleRow], total: 1 })),
        uploadFile: vi.fn(actionResult),
        downloadFile: vi.fn(() => ok(new Blob())),
        previewFile: vi.fn(() => ok('http://preview')),
        deleteObject: vi.fn(actionResult),
        batchDeleteObjects: vi.fn(actionResult),
    },
    job: {
        getJobById: vi.fn(detailResult),
        listJob: vi.fn(listResult),
        pageJob: vi.fn(pageResult),
        saveJob: vi.fn(actionResult),
        updateJob: vi.fn(actionResult),
        deleteJob: vi.fn(actionResult),
        batchDeleteJob: vi.fn(actionResult),
        changeJobStatus: vi.fn(actionResult),
        runJob: vi.fn(actionResult),
    },
    jobLog: {
        pageJobLog: vi.fn(pageResult),
        cleanJobLog: vi.fn(actionResult),
    },
    redis: {
        getRedisInfo: vi.fn(() => ok({ version: '7.0.0', usedMemory: '1M' })),
        getCommandStats: vi.fn(() => ok([{ command: 'get', count: 1 }])),
        getRedisKeyTree: vi.fn(() => ok([{ name: 'demo', children: [] }])),
        pageRedisKeys: vi.fn(() => ok({ records: [{ key: 'demo:1', type: 'string' }], total: 1 })),
        getRedisKeyDetail: vi.fn(() => ok({ key: 'demo:1', type: 'string', value: '1' })),
        deleteRedisKeys: vi.fn(actionResult),
    },
}

export function resetApiMock() {
    Object.values(apiMock).forEach((module) => {
        Object.values(module).forEach((fn) => {
            if (typeof fn?.mockClear === 'function') {
                fn.mockClear()
            }
        })
    })
}
