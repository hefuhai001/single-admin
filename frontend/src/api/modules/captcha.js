import request from '@/utils/requestUtil.js'

/**
 * 获取滑块验证码
 */
export function getSliderCaptcha() {
    return request.get('/captcha/slider')
}

/**
 * 校验滑块位置
 */
export function verifySlider(data) {
    return request.post('/captcha/slider/verify', data)
}

/**
 * 发送邮箱验证码（需先通过滑块验证）
 */
export function sendEmailCode(data) {
    return request.post('/captcha/email/send', data)
}
