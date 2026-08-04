import request from '@/utils/requestUtil.js'

/**
 * Get overview statistics for dashboard cards
 */
export function getOverview() {
    return request.get(`/admin/stats/overview`)
}

/**
 * Get user trend data (new/active users by month)
 */
export function getUserTrend() {
    return request.get(`/admin/stats/user-trend`)
}

/**
 * Get patient gender distribution
 */
export function getPatientGender() {
    return request.get(`/admin/stats/patient-gender`)
}

/**
 * Get questionnaire status distribution
 */
export function getQuestionnaireStatus() {
    return request.get(`/admin/stats/questionnaire-status`)
}

/**
 * Get answer distribution by group
 */
export function getAnswerDistribution() {
    return request.get(`/admin/stats/answer-distribution`)
}
