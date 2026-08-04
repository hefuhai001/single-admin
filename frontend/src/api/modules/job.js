import request from '@/utils/requestUtil.js'

export function getJobById(id) {
    return request.get(`/system/job/` + id)
}

export function listJob() {
    return request.get(`/system/job/list`)
}

export function pageJob(params) {
    return request.get(`/system/job/page`, { params })
}

export function saveJob(params) {
    return request.post(`/system/job`, params)
}

export function updateJob(params) {
    return request.put(`/system/job`, params)
}

export function deleteJob(id) {
    return request.delete(`/system/job/` + id)
}

export function batchDeleteJob(ids) {
    return request.delete(`/system/job/batch`, { data: ids })
}

export function changeJobStatus(params) {
    return request.put(`/system/job/changeStatus`, null, { params })
}

export function runJob(id) {
    return request.post(`/system/job/run/` + id)
}
