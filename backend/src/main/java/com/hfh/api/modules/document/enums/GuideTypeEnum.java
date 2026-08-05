package com.hfh.api.modules.document.enums;

/**
 * 指南类型枚举
 */
public enum GuideTypeEnum {

    GENERAL("general", "通用指南"),
    OPERATION("operation", "操作指南"),
    QUALITY("quality", "质控指南"),
    OTHER("other", "其他");

    private final String code;
    private final String description;

    GuideTypeEnum(String code, String description) {
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
    public static GuideTypeEnum fromCode(String code) {
        for (GuideTypeEnum type : GuideTypeEnum.values()) {
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