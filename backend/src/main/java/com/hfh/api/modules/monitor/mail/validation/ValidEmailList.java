package com.hfh.api.modules.monitor.mail.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 邮箱列表格式验证注解
 * <p>用于验证 List&lt;String&gt; 中的每个元素是否符合邮箱格式</p>
 * <p>解决标准 @Email 注解无法直接用于集合类型的问题</p>
 */
@Documented
@Constraint(validatedBy = ValidEmailListValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmailList {

    /**
     * 验证失败时的错误提示信息
     */
    String message() default "邮箱列表中存在格式不正确的邮箱";

    /**
     * 验证分组（用于分组校验场景）
     */
    Class<?>[] groups() default {};

    /**
     * 负载类型（通常无需修改）
     */
    Class<? extends Payload>[] payload() default {};
}
