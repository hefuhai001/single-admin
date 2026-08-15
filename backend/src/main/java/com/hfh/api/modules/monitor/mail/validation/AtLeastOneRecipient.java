package com.hfh.api.modules.monitor.mail.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 联合收件人验证注解
 * <p>确保 to（单收件人）和 toList（多收件人）至少填写一项</p>
 * <p>避免单收件人场景下 toList 的 @NotEmpty 导致校验失败</p>
 */
@Documented
@Constraint(validatedBy = AtLeastOneRecipientValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AtLeastOneRecipient {

    /**
     * 验证失败时的错误提示信息
     */
    String message() default "收件人或收件人列表至少填写一项";

    /**
     * 验证分组（用于分组校验场景）
     */
    Class<?>[] groups() default {};

    /**
     * 负载类型（通常无需修改）
     */
    Class<? extends Payload>[] payload() default {};
}
