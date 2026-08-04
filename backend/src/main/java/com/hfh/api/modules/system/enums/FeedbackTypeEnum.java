package com.hfh.api.modules.system.enums;

/**
 * 反馈类型枚举
 */
public enum FeedbackTypeEnum {

    QUESTION("question", "疑问"),
    SUGGESTION("suggestion", "建议"),
    COMPLAINT("complaint", "投诉"),
    ISSUE("issue", "问题报告"),
    OTHER("other", "其他");

    private final String code;
    private final String description;

    FeedbackTypeEnum(String code, String description) {
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
    public static FeedbackTypeEnum fromCode(String code) {
        for (FeedbackTypeEnum type : FeedbackTypeEnum.values()) {
            if (type.code.equals(code)) {
                return type;
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