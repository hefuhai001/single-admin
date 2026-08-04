import request from '@/utils/requestUtil.js'

/**
 * login
 * @param {string} username
 * @param {string} password
 * @returns
 */
export function login(username, password) {
    return request.post(`/auth/login?username=${username}&password=${password}`)
}

/**
 * 刷新 accessToken
 * @param {string} refreshToken
 * @returns
 */
export function refresh(refreshToken) {
    return request.post(`/auth/refresh?refreshToken=${refreshToken}`)
}

/**
 * logout
 * @returns
 */
export function logout() {
    return request.post(`/auth/logout`)
}

/**
 * isLogin
 * @returns
 */
export function isLogin() {
    return request.get(`/auth/isLogin`)
}

/**
 * emailLogin
 * @param {string} email
 * @param {string} code
 * @returns
 */
export function emailLogin(email, code) {
    return request.get(`/login?email=${email}&code=${code}`)
}

/**
 * emailCode
 * @param {string} email
 * @returns
 */
export function emailCode(email) {
    return request.get(`/code?email=${email}`)
}

/**
 * profile
 * @returns
 */
export function profile() {
    return request.get(`/auth/profile`)
}
