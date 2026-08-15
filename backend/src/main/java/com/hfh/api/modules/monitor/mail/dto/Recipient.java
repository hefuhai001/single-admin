package com.hfh.api.modules.monitor.mail.dto;

import java.util.List;

/**
 * 收件人接口
 * <p>用于统一单收件人和多收件人的获取</p>
 */
public interface Recipient {

    /**
     * 获取单收件人邮箱
     *
     * @return 单收件人邮箱，可能为 null
     */
    String getTo();

    /**
     * 获取多收件人邮箱列表
     *
     * @return 多收件人列表，可能为 null 或空列表
     */
    List<String> getToList();
}
