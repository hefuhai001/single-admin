import * as feedbacks from '@/api/modules/feedbacks.js'
import * as guides from '@/api/modules/guides.js'
import * as institutions from '@/api/modules/institutions.js'
import * as permission from '@/api/modules/permission.js'
import * as role from '@/api/modules/role.js'
import * as user from '@/api/modules/user.js'
import * as auth from '@/api/modules/auth.js'

import * as minio from '@/api/modules/minio.js'

import * as job from '@/api/modules/job.js'
import * as jobLog from '@/api/modules/jobLog.js'

const api = {
    permission,
    role,
    user,
    auth,
    feedbacks,
    guides,
    institutions,

    minio,

    job,
    jobLog,
}

export default api
