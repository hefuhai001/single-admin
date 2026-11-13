import { beforeEach, describe, expect, it, vi } from 'vitest'

const { requestMock } = vi.hoisted(() => ({
    requestMock: {
        get: vi.fn(() => 'response'),
        post: vi.fn(() => 'response'),
        put: vi.fn(() => 'response'),
        delete: vi.fn(() => 'response'),
    },
}))

vi.mock('@/utils/requestUtil.js', () => ({ default: requestMock }))

import * as auth from '@/api/modules/auth.js'
import * as captcha from '@/api/modules/captcha.js'
import * as email from '@/api/modules/email.js'
import * as stats from '@/api/modules/stats.js'
import * as feedbacks from '@/api/modules/document/feedbacks.js'
import * as guides from '@/api/modules/document/guides.js'
import * as job from '@/api/modules/monitor/job.js'
import * as jobLog from '@/api/modules/monitor/jobLog.js'
import * as online from '@/api/modules/monitor/online.js'
import * as oss from '@/api/modules/monitor/oss.js'
import * as redis from '@/api/modules/monitor/redis.js'
import * as config from '@/api/modules/system/config.js'
import * as dictData from '@/api/modules/system/dictData.js'
import * as dictType from '@/api/modules/system/dictType.js'
import * as institutions from '@/api/modules/system/institutions.js'
import * as permission from '@/api/modules/system/permission.js'
import * as role from '@/api/modules/system/role.js'
import * as user from '@/api/modules/system/user.js'

const specs = [
    {
        name: 'auth',
        module: auth,
        cases: [
            ['login', ['admin', '123456', 'dev-1'], 'post', '/auth/login?username=admin&password=123456&deviceId=dev-1'],
            ['refresh', ['rt'], 'post', '/auth/refresh?refreshToken=rt'],
            ['logout', [], 'post', '/auth/logout'],
            ['isLogin', [], 'get', '/auth/isLogin'],
            ['emailLogin', ['a@b.com', '1234'], 'get', '/login?email=a@b.com&code=1234'],
            ['emailCode', ['a@b.com'], 'get', '/code?email=a@b.com'],
            ['profile', [], 'get', '/auth/profile'],
            ['register', [{ username: 'u' }], 'post', '/auth/register', { username: 'u' }],
        ],
    },
    {
        name: 'captcha',
        module: captcha,
        cases: [
            ['getSliderCaptcha', [], 'get', '/captcha/slider'],
            ['verifySlider', [{ position: 10 }], 'post', '/captcha/slider/verify', { position: 10 }],
            ['sendEmailCode', [{ email: 'a@b.com' }], 'post', '/captcha/email/send', { email: 'a@b.com' }],
        ],
    },
    {
        name: 'email',
        module: email,
        cases: [
            ['sendSimpleEmail', [{ to: 'a@b.com' }], 'post', '/admin/email/simple', { to: 'a@b.com' }],
            ['sendHtmlEmail', [{ to: 'a@b.com' }], 'post', '/admin/email/html', { to: 'a@b.com' }],
            ['sendTemplateEmail', [{ templateName: 'welcome' }], 'post', '/admin/email/template', { templateName: 'welcome' }],
            ['sendBatchEmails', [[{ to: 'a@b.com' }]], 'post', '/admin/email/batch', [{ to: 'a@b.com' }]],
        ],
    },
    {
        name: 'stats',
        module: stats,
        cases: [
            ['getOverview', [], 'get', '/admin/stats/overview'],
            ['getUserTrend', [], 'get', '/admin/stats/user-trend'],
            ['getPatientGender', [], 'get', '/admin/stats/patient-gender'],
            ['getQuestionnaireStatus', [], 'get', '/admin/stats/questionnaire-status'],
            ['getAnswerDistribution', [], 'get', '/admin/stats/answer-distribution'],
        ],
    },
    {
        name: 'feedbacks',
        module: feedbacks,
        cases: [
            ['getFeedbacksById', [3], 'get', '/admin/feedbacks/3'],
            ['listFeedbacks', [], 'get', '/admin/feedbacks/list'],
            ['pageFeedbacks', [{ current: 1 }], 'get', '/admin/feedbacks/page', undefined, { params: { current: 1 } }],
            ['saveFeedbacks', [{ id: 1 }], 'post', '/admin/feedbacks', { id: 1 }],
            ['updateFeedbacks', [{ id: 1 }], 'put', '/admin/feedbacks', { id: 1 }],
            ['deleteFeedbacks', [3], 'delete', '/admin/feedbacks/3'],
            ['batchDeleteFeedbacks', [[1, 2]], 'delete', '/admin/feedbacks/batch', undefined, { data: [1, 2] }],
        ],
    },
    {
        name: 'guides',
        module: guides,
        cases: [
            ['getGuidesById', [3], 'get', '/admin/guides/3'],
            ['listGuides', [], 'get', '/admin/guides/list'],
            ['pageGuides', [{ current: 1 }], 'get', '/admin/guides/page', undefined, { params: { current: 1 } }],
            ['saveGuides', [{ id: 1 }], 'post', '/admin/guides', { id: 1 }],
            ['updateGuides', [{ id: 1 }], 'put', '/admin/guides', { id: 1 }],
            ['deleteGuides', [3], 'delete', '/admin/guides/3'],
            ['batchDeleteGuides', [[1, 2]], 'delete', '/admin/guides/batch', undefined, { data: [1, 2] }],
        ],
    },
    {
        name: 'job',
        module: job,
        cases: [
            ['getJobById', [3], 'get', '/system/job/3'],
            ['listJob', [], 'get', '/system/job/list'],
            ['pageJob', [{ current: 1 }], 'get', '/system/job/page', undefined, { params: { current: 1 } }],
            ['saveJob', [{ id: 1 }], 'post', '/system/job', { id: 1 }],
            ['updateJob', [{ id: 1 }], 'put', '/system/job', { id: 1 }],
            ['deleteJob', [3], 'delete', '/system/job/3'],
            ['batchDeleteJob', [[1, 2]], 'delete', '/system/job/batch', undefined, { data: [1, 2] }],
            ['changeJobStatus', [{ id: 1, status: 1 }], 'put', '/system/job/changeStatus', null, { params: { id: 1, status: 1 } }],
            ['runJob', [3], 'post', '/system/job/run/3'],
        ],
    },
    {
        name: 'jobLog',
        module: jobLog,
        cases: [
            ['pageJobLog', [{ current: 1 }], 'get', '/system/jobLog/page', undefined, { params: { current: 1 } }],
            ['cleanJobLog', [], 'delete', '/system/jobLog/clean'],
        ],
    },
    {
        name: 'online',
        module: online,
        cases: [
            ['getOnlineUsers', [], 'get', '/admin/monitor/online'],
            ['kickOnlineUser', ['a b'], 'delete', '/admin/monitor/online/a%20b'],
        ],
    },
    {
        name: 'oss',
        module: oss,
        cases: [
            ['listBuckets', [], 'get', '/admin/oss/buckets'],
            ['createBucket', ['demo'], 'post', '/admin/oss/buckets/demo'],
            ['deleteBucket', ['demo'], 'delete', '/admin/oss/buckets/demo'],
            ['listObjects', [{ bucket: 'demo' }], 'get', '/admin/oss/objects', undefined, { params: { bucket: 'demo' } }],
            ['uploadFile', ['form-data'], 'post', '/admin/oss/objects/upload', 'form-data', { headers: { 'Content-Type': 'multipart/form-data' } }],
            ['downloadFile', [{ bucket: 'demo' }], 'get', '/admin/oss/objects/download', undefined, { params: { bucket: 'demo' }, responseType: 'blob' }],
            ['previewFile', [{ bucket: 'demo' }], 'get', '/admin/oss/objects/preview', undefined, { params: { bucket: 'demo' } }],
            ['deleteObject', [{ bucket: 'demo' }], 'delete', '/admin/oss/objects', undefined, { params: { bucket: 'demo' } }],
            ['batchDeleteObjects', ['demo', ['a.txt']], 'delete', '/admin/oss/objects/batch', undefined, { params: { bucket: 'demo' }, data: ['a.txt'] }],
        ],
    },
    {
        name: 'redis',
        module: redis,
        cases: [
            ['getRedisInfo', [], 'get', '/admin/monitor/redis/info'],
            ['getCommandStats', [], 'get', '/admin/monitor/redis/command-stats'],
            ['getRedisKeyTree', [], 'get', '/admin/monitor/redis/keys/tree'],
            ['pageRedisKeys', [{ pattern: '*' }], 'get', '/admin/monitor/redis/keys', undefined, { params: { pattern: '*' } }],
            ['getRedisKeyDetail', ['a:b'], 'get', '/admin/monitor/redis/keys/a%3Ab'],
            ['deleteRedisKeys', [['a', 'b']], 'delete', '/admin/monitor/redis/keys', undefined, { data: ['a', 'b'] }],
        ],
    },
    {
        name: 'config',
        module: config,
        cases: [
            ['getConfigById', [3], 'get', '/system/config/3'],
            ['listConfig', [], 'get', '/system/config/list'],
            ['pageConfig', [{ current: 1 }], 'get', '/system/config/page', undefined, { params: { current: 1 } }],
            ['saveConfig', [{ id: 1 }], 'post', '/system/config', { id: 1 }],
            ['updateConfig', [{ id: 1 }], 'put', '/system/config', { id: 1 }],
            ['deleteConfig', [3], 'delete', '/system/config/3'],
            ['batchDeleteConfig', [[1, 2]], 'delete', '/system/config/batch', undefined, { data: [1, 2] }],
        ],
    },
    {
        name: 'dictData',
        module: dictData,
        cases: [
            ['getDictDataById', [3], 'get', '/dict/dict-data/3'],
            ['listDictData', [], 'get', '/dict/dict-data/list'],
            ['pageDictData', [{ current: 1 }], 'get', '/dict/dict-data/page', undefined, { params: { current: 1 } }],
            ['saveDictData', [{ id: 1 }], 'post', '/dict/dict-data', { id: 1 }],
            ['updateDictData', [{ id: 1 }], 'put', '/dict/dict-data', { id: 1 }],
            ['deleteDictData', [3], 'delete', '/dict/dict-data/3'],
            ['batchDeleteDictData', [[1, 2]], 'delete', '/dict/dict-data/batch', undefined, { data: [1, 2] }],
        ],
    },
    {
        name: 'dictType',
        module: dictType,
        cases: [
            ['getDictTypeById', [3], 'get', '/dict/dict-type/3'],
            ['listDictType', [], 'get', '/dict/dict-type/list'],
            ['pageDictType', [{ current: 1 }], 'get', '/dict/dict-type/page', undefined, { params: { current: 1 } }],
            ['saveDictType', [{ id: 1 }], 'post', '/dict/dict-type', { id: 1 }],
            ['updateDictType', [{ id: 1 }], 'put', '/dict/dict-type', { id: 1 }],
            ['deleteDictType', [3], 'delete', '/dict/dict-type/3'],
            ['batchDeleteDictType', [[1, 2]], 'delete', '/dict/dict-type/batch', undefined, { data: [1, 2] }],
        ],
    },
    {
        name: 'institutions',
        module: institutions,
        cases: [
            ['treeInstitutions', [], 'get', '/admin/institutions/list-tree'],
            ['getInstitutionsById', [3], 'get', '/admin/institutions/3'],
            ['listInstitutions', [], 'get', '/admin/institutions/list'],
            ['pageInstitutions', [{ current: 1 }], 'get', '/admin/institutions/page', undefined, { params: { current: 1 } }],
            ['saveInstitutions', [{ id: 1 }], 'post', '/admin/institutions', { id: 1 }],
            ['updateInstitutions', [{ id: 1 }], 'put', '/admin/institutions', { id: 1 }],
            ['deleteInstitutions', [3], 'delete', '/admin/institutions/3'],
            ['batchDeleteInstitutions', [[1, 2]], 'delete', '/admin/institutions/batch', undefined, { data: [1, 2] }],
        ],
    },
    {
        name: 'permission',
        module: permission,
        cases: [
            ['pagePermission', [{ current: 1 }], 'get', '/admin/permission/page', undefined, { params: { current: 1 } }],
            ['getPermissionById', [3], 'get', '/admin/permission/3'],
            ['savePermission', [{ id: 1 }], 'post', '/admin/permission', { id: 1 }],
            ['updatePermission', [{ id: 1 }], 'put', '/admin/permission', { id: 1 }],
            ['deletePermission', [3], 'delete', '/admin/permission/3'],
            ['batchDeletePermission', [[1, 2]], 'delete', '/admin/permission/batch', undefined, { data: [1, 2] }],
            ['treePermission', [], 'get', '/admin/permission/list-tree'],
        ],
    },
    {
        name: 'role',
        module: role,
        cases: [
            ['pageRole', [{ current: 1 }], 'get', '/admin/role/page', undefined, { params: { current: 1 } }],
            ['getRoleById', [3], 'get', '/admin/role/3'],
            ['saveRole', [{ id: 1 }], 'post', '/admin/role', { id: 1 }],
            ['updateRole', [{ id: 1 }], 'put', '/admin/role', { id: 1 }],
            ['deleteRole', [3], 'delete', '/admin/role/3'],
            ['batchDeleteRole', [[1, 2]], 'delete', '/admin/role/batch', undefined, { data: [1, 2] }],
            ['list', [], 'get', '/admin/role/list'],
        ],
    },
    {
        name: 'user',
        module: user,
        cases: [
            ['pageUser', [{ current: 1 }], 'get', '/admin/user/page', undefined, { params: { current: 1 } }],
            ['getUserById', [3], 'get', '/admin/user/3'],
            ['saveUser', [{ id: 1 }], 'post', '/admin/user', { id: 1 }],
            ['updateUser', [{ id: 1 }], 'put', '/admin/user', { id: 1 }],
            ['deleteUser', [3], 'delete', '/admin/user/3'],
            ['batchDeleteUser', [[1, 2]], 'delete', '/admin/user/batch', undefined, { data: [1, 2] }],
            ['routesUser', [], 'get', '/admin/user/routes'],
            ['permissionsUser', [], 'get', '/admin/user/permissions'],
        ],
    },
]

describe('api modules', () => {
    beforeEach(() => {
        requestMock.get.mockClear()
        requestMock.post.mockClear()
        requestMock.put.mockClear()
        requestMock.delete.mockClear()
    })

    specs.forEach(({ name, module, cases }) => {
        describe(name, () => {
            cases.forEach(([fnName, args, method, url, expectedData, expectedConfig]) => {
                it(`${fnName} 调用 ${method.toUpperCase()} ${url}`, () => {
                    expect(typeof module[fnName]).toBe('function')

                    const result = module[fnName](...args)
                    expect(result).toBe('response')

                    const mockFn = requestMock[method]
                    expect(mockFn).toHaveBeenCalledTimes(1)

                    const call = mockFn.mock.calls[0]
                    expect(call[0]).toBe(url)

                    if (expectedData !== undefined) {
                        expect(call[1]).toEqual(expectedData)
                    }
                    if (expectedConfig !== undefined) {
                        const configIndex = method === 'get' || method === 'delete' ? 1 : 2
                        expect(call[configIndex]).toEqual(expectedConfig)
                    }
                })
            })
        })
    })
})
