package com.hfh.api.modules.system.enums;

/**
 * 状态枚举
 */
public enum GuideStatusEnum {

    ACTIVE("active", "有效"),
    EXPIRED("expired", "已过期"),
    ARCHIVED("archived", "已归档");

    private final String code;
    private final String description;

    GuideStatusEnum(String code, String description) {
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
    public static GuideStatusEnum fromCode(String code) {
        for (GuideStatusEnum status : GuideStatusEnum.values()) {
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