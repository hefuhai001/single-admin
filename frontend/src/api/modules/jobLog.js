import request from '@/utils/requestUtil.js'

export function pageJobLog(params) {
    return request.get(`/system/jobLog/page`, { params })
}

export function cleanJobLog() {
    return request.delete(`/system/jobLog/clean`)
}
