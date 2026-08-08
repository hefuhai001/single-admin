package com.hfh.api.config;

/**
 * 系统配置 key 统一管理
 * <p>
 * 集中维护所有从 sys_config 表读取的配置 key，以及认证、验证码相关的 Redis key 前缀，
 * 避免配置字符串散落在各个 Service 中难以维护。
 * </p>
 */
public final class SysConfigKeys {

    private SysConfigKeys() {
    }

    // ================== 注册相关配置 ==================

    /** 是否开启注册 */
    public static final String REGISTER_ENABLE = "register.enable";

    /** 注册是否需要邮箱验证码 */
    public static final String REGISTER_VERIFY_CODE_ENABLE = "register.verify_code.enable";

    /** 注册默认角色编码 */
    public static final String REGISTER_DEFAULT_ROLE = "register.default_role";

    /** 注册默认机构编码 */
    public static final String REGISTER_DEFAULT_INSTITUTION = "register.default_institution";

    // ================== 邮箱相关配置 ==================

    /** 是否开启邮箱验证 */
    public static final String EMAIL_VERIFY_ENABLE = "email.verify.enable";

    /** SMTP 服务器地址 */
    public static final String EMAIL_SMTP_HOST = "email.smtp.host";

    /** SMTP 端口 */
    public static final String EMAIL_SMTP_PORT = "email.smtp.port";

    /** 发件人邮箱 */
    public static final String EMAIL_SENDER = "email.sender";

    /** 发件人授权码/密码 */
    public static final String EMAIL_SENDER_PASSWORD = "email.sender.password";

    /** 邮箱验证码有效期（秒） */
    public static final String EMAIL_VERIFY_EXPIRE = "email.verify.expire";

    /** 邮箱验证码发送间隔（秒） */
    public static final String EMAIL_SEND_LIMIT = "email.send.limit";

    // ================== 验证码相关配置 ==================

    /** 滑块验证码有效期（秒） */
    public static final String CAPTCHA_SLIDER_EXPIRE = "captcha.slider.expire";

    // ================== 登录相关配置 ==================

    /** 登录最大尝试次数 */
    public static final String LOGIN_MAX_ATTEMPTS = "login.max_attempts";

    /** 账号锁定时长（秒） */
    public static final String LOGIN_LOCK_DURATION = "login.lock_duration";

    /** 同一账号最多同时在线设备数，默认 3 */
    public static final String LOGIN_MAX_ONLINE_DEVICES = "login.max_online_devices";

    // ================== Redis key 前缀 ==================

    /** 滑块验证码坐标 Redis key 前缀 */
    public static final String REDIS_CAPTCHA_SLIDER_PREFIX = "captcha:slider:";

    /** 滑块验证通过 token Redis key 前缀 */
    public static final String REDIS_CAPTCHA_VERIFIED_PREFIX = "captcha:verified:";

    /** 邮箱验证码 Redis key 前缀 */
    public static final String REDIS_CAPTCHA_EMAIL_PREFIX = "captcha:email:";

    /** 邮箱验证码发送频率限制 Redis key 前缀 */
    public static final String REDIS_CAPTCHA_EMAIL_LIMIT_PREFIX = "captcha:email:limit:";

    /** 登录失败次数 Redis key 前缀 */
    public static final String REDIS_LOGIN_FAIL_PREFIX = "login:fail:";

    /** 账号锁定 Redis key 前缀 */
    public static final String REDIS_LOGIN_LOCK_PREFIX = "login:lock:";
}
