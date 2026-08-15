package com.hfh.api.modules.monitor.mail.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 邮箱列表格式验证器实现
 * <p>遍历 List&lt;String&gt; 中的每个元素，检查是否符合邮箱格式</p>
 */
public class ValidEmailListValidator implements ConstraintValidator<ValidEmailList, List<String>> {

    /**
     * 邮箱格式正则表达式
     * <p>匹配规则：字母数字+_.- @ 字母数字.-</p>
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    /**
     * 执行验证逻辑
     *
     * @param emails  待验证的邮箱列表
     * @param context 验证上下文，用于构建具体错误信息
     * @return true-所有邮箱格式正确或列表为空，false-存在格式错误的邮箱
     */
    @Override
    public boolean isValid(List<String> emails, ConstraintValidatorContext context) {
        // 空列表由调用方其他校验处理，此处视为通过
        if (emails == null || emails.isEmpty()) {
            return true;
        }

        boolean allValid = true;
        for (String email : emails) {
            if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
                allValid = false;
                // 禁用默认错误信息，添加具体的错误邮箱地址
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "邮箱格式不正确: " + email
                ).addConstraintViolation();
            }
        }
        return allValid;
    }
}
