package com.hfh.api.modules.monitor.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

import com.hfh.api.modules.monitor.mail.validation.AtLeastOneRecipient;
import com.hfh.api.modules.monitor.mail.validation.ValidEmailList;

/**
 * 邮件发送请求对象
 * <p>支持单收件人和多收件人（群发）两种模式，二者至少填一项</p>
 */
@Data
@AtLeastOneRecipient(message = "收件人或收件人列表至少填写一项")
@Schema(description = "邮件发送请求对象")
public class EmailRequest implements Recipient {

    /**
     * 单收件人邮箱地址
     * <p>适用于发送给单个收件人的场景</p>
     * <p>格式要求：符合标准邮箱格式，如 example@qq.com</p>
     */
    @Email(message = "收件人邮箱格式不正确")
    @Schema(description = "单收件人邮箱地址", example = "example@qq.com")
    private String to;

    /**
     * 多收件人邮箱地址列表（群发）
     * <p>适用于批量发送给多个收件人的场景，所有收件人互相可见</p>
     * <p>每个邮箱地址均需符合标准邮箱格式</p>
     */
    @ValidEmailList(message = "收件人列表中存在格式不正确的邮箱")
    @Schema(description = "多收件人邮箱地址列表（群发）")
    private List<String> toList;

    /**
     * 抄送邮箱地址列表
     * <p>所有收件人和抄送人互相可见</p>
     * <p>可选字段，为空表示不抄送</p>
     */
    @ValidEmailList(message = "抄送列表中存在格式不正确的邮箱")
    @Schema(description = "抄送邮箱地址列表")
    private List<String> cc;

    /**
     * 密送邮箱地址列表
     * <p>密送人之间互相不可见，主收件人也不会看到密送人</p>
     * <p>可选字段，为空表示不密送</p>
     */
    @ValidEmailList(message = "密送列表中存在格式不正确的邮箱")
    @Schema(description = "密送邮箱地址列表")
    private List<String> bcc;

    /**
     * 邮件主题/标题
     * <p>不能为空</p>
     */
    @NotBlank(message = "邮件主题不能为空")
    @Schema(description = "邮件主题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String subject;

    /**
     * 邮件正文内容
     * <p>支持纯文本或 HTML 格式，由 {@link #html} 字段控制</p>
     * <p>不能为空</p>
     */
    @NotBlank(message = "邮件内容不能为空")
    @Schema(description = "邮件正文内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    /**
     * 是否为 HTML 格式邮件
     * <p>true：content 字段为 HTML 代码，邮件客户端会渲染显示</p>
     * <p>false：content 字段为纯文本，直接显示（默认）</p>
     */
    @Schema(description = "是否为 HTML 格式邮件", example = "false")
    private boolean html = false;

    /**
     * 附件文件路径列表
     * <p>填写服务器本地绝对路径，如 /tmp/report.pdf</p>
     * <p>可选字段，为空表示无附件</p>
     * <p>注意：文件必须存在且可读，否则发送时会跳过不存在的附件并记录警告日志</p>
     */
    @Schema(description = "附件文件路径列表（服务器本地绝对路径）")
    private List<String> attachments;
}
