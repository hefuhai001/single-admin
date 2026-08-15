package com.hfh.api.modules.monitor.mail.validation;

import com.hfh.api.modules.monitor.mail.dto.Recipient;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 联合收件人验证器实现
 * <p>检查实现了 {@link Recipient} 接口的对象中 to 或 toList 至少有一个有效值</p>
 */
public class AtLeastOneRecipientValidator implements ConstraintValidator<AtLeastOneRecipient, Recipient> {

    /**
     * 执行验证逻辑
     *
     * @param recipient 实现了 Recipient 接口的对象
     * @param context   验证上下文，可用于自定义错误信息
     * @return true-验证通过（至少有一个收件人），false-验证失败
     */
    @Override
    public boolean isValid(Recipient recipient, ConstraintValidatorContext context) {
        if (recipient == null) {
            return false;
        }

        boolean hasTo = recipient.getTo() != null && !recipient.getTo().isBlank();
        boolean hasToList = recipient.getToList() != null && !recipient.getToList().isEmpty();

        return hasTo || hasToList;
    }
}
