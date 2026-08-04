package com.hfh.api.modules.system.enums;

/**
 * 优先级枚举
 */
public enum FeedbackPriorityEnum {

    LOW("low", "低"),
    MEDIUM("medium", "中"),
    HIGH("high", "高"),
    URGENT("urgent", "紧急");

    private final String code;
    private final String description;

    FeedbackPriorityEnum(String code, String description) {
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
    public static FeedbackPriorityEnum fromCode(String code) {
        for (FeedbackPriorityEnum priority : FeedbackPriorityEnum.values()) {
            if (priority.code.equals(code)) {
                return priority;
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