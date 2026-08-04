package com.hfh.api.modules.system.enums;

/**
 * 反馈状态枚举
 */
public enum FeedbackStatusEnum {

    PENDING("pending", "待处理"),
    PROCESSING("processing", "处理中"),
    RESOLVED("resolved", "已解决"),
    CLOSED("closed", "已关闭"),
    REJECTED("rejected", "已驳回");

    private final String code;
    private final String description;

    FeedbackStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 代码值
     * @return 对应的枚举，找不到返回null
     */
    public static FeedbackStatusEnum fromCode(String code) {
        for (FeedbackStatusEnum status : FeedbackStatusEnum.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 校验code是否有效
     *
     * @param code 代码值
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    @Override
    public String toString() {
        return code;
    }
}