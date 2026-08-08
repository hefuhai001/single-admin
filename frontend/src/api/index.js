import * as auth from '@/api/modules/auth.js'
import * as feedbacks from '@/api/modules/document/feedbacks.js'
import * as guides from '@/api/modules/document/guides.js'
import * as institutions from '@/api/modules/system/institutions.js'
import * as permission from '@/api/modules/system/permission.js'
import * as role from '@/api/modules/system/role.js'
import * as user from '@/api/modules/system/user.js'
import * as dictType from '@/api/modules/system/dictType'
import * as dictData from '@/api/modules/system/dictData'
import * as config from '@/api/modules/system/config.js'
import * as captcha from '@/api/modules/captcha.js'

import * as online from '@/api/modules/monitor/online.js'
import * as minio from '@/api/modules/monitor/minio.js'
import * as job from '@/api/modules/monitor/job.js'
import * as jobLog from '@/api/modules/monitor/jobLog.js'

const api = {
    auth,
    permission,
    role,
    user,
    feedbacks,
    guides,
    institutions,
    dictType,
    dictData,
    config,
    captcha,

    online,
    minio,
    job,
    jobLog,
}

export default api
