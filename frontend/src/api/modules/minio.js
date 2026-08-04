import request from '@/utils/requestUtil.js'

/**
 * 查询存储桶列表
 * @returns {Promise}
 */
export function listBuckets() {
    return request.get('/admin/minio/buckets')
}

/**
 * 创建存储桶
 * @param {string} bucketName
 * @returns {Promise}
 */
export function createBucket(bucketName) {
    return request.post('/admin/minio/buckets/' + bucketName)
}

/**
 * 删除存储桶
 * @param {string} bucketName
 * @returns {Promise}
 */
export function deleteBucket(bucketName) {
    return request.delete('/admin/minio/buckets/' + bucketName)
}

/**
 * 分页查询对象列表
 * @param {object} params
 * @returns {Promise}
 */
export function listObjects(params) {
    return request.get('/admin/minio/objects', { params })
}

/**
 * 上传文件
 * @param {FormData} data
 * @returns {Promise}
 */
export function uploadFile(data) {
    return request.post('/admin/minio/objects/upload', data, {
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    })
}

/**
 * 下载文件
 * @param {object} params
 * @returns {Promise}
 */
export function downloadFile(params) {
    return request.get('/admin/minio/objects/download', {
        params,
        responseType: 'blob',
    })
}

/**
 * 获取文件预览地址
 * @param {object} params
 * @returns {Promise}
 */
export function previewFile(params) {
    return request.get('/admin/minio/objects/preview', { params })
}

/**
 * 删除对象
 * @param {object} params
 * @returns {Promise}
 */
export function deleteObject(params) {
    return request.delete('/admin/minio/objects', { params })
}

/**
 * 批量删除对象
 * @param {string} bucket
 * @param {string[]} objectNames
 * @returns {Promise}
 */
export function batchDeleteObjects(bucket, objectNames) {
    return request.delete('/admin/minio/objects/batch', {
        params: { bucket },
        data: objectNames,
    })
}
