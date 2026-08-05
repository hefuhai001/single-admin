package com.hfh.api.modules.document.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 机构反馈表
 * </p>
 *
 * @author xiaoHe
 * @since 2026-04-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("doc_feedbacks")
public class FeedbacksEntity implements Serializable {


    /**
     * 反馈ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 机构ID
     */
    @TableField("institution_id")
    private Long institutionId;

    /**
     * 反馈类型: question-疑问, suggestion-建议, complaint-投诉, issue-问题报告, other-其他
     */
    @TableField("feedback_type")
    private String feedbackType;

    /**
     * 反馈标题
     */
    @TableField("feedback_title")
    private String feedbackTitle;

    /**
     * 反馈内容
     */
    @TableField("feedback_content")
    private String feedbackContent;

    /**
     * 附件URL
     */
    @TableField("attachment_url")
    private String attachmentUrl;

    /**
     * 优先级: low-低, medium-中, high-高, urgent-紧急
     */
    @TableField("priority")
    private String priority;

    /**
     * 状态: pending-待处理, processing-处理中, resolved-已解决, closed-已关闭, rejected-已驳回
     */
    @TableField("status")
    private String status;

    /**
     * 回复内容
     */
    @TableField("reply_content")
    private String replyContent;

    /**
     * 回复人ID
     */
    @TableField("replied_by")
    private Long repliedBy;

    /**
     * 回复时间
     */
    @TableField("replied_at")
    private Date repliedAt;

    /**
     * 反馈人ID
     */
    @TableField("feedback_by")
    private Long feedbackBy;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;

    /**
     * 软删除
     */
    @TableField("is_deleted")
    private Boolean isDeleted;
}
