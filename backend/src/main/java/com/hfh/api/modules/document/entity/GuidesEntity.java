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
 * 机构指南表
 * </p>
 *
 * @author xiaoHe
 * @since 2026-04-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("doc_guides")
public class GuidesEntity implements Serializable {


    /**
     * 指南ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 机构ID
     */
    @TableField("institution_id")
    private Long institutionId;

    /**
     * 指南标题
     */
    @TableField("guide_title")
    private String guideTitle;

    /**
     * 指南内容(富文本)
     */
    @TableField("guide_content")
    private String guideContent;

    /**
     * 指南附件URL(PDF/Word等)
     */
    @TableField("guide_file_url")
    private String guideFileUrl;

    /**
     * 指南类型: general-通用指南, operation-操作指南, quality-质控指南, other-其他
     */
    @TableField("guide_type")
    private String guideType;

    /**
     * 状态: active-有效, expired-已过期, archived-已归档
     */
    @TableField("status")
    private String status;

    /**
     * 发布时间
     */
    @TableField("published_at")
    private Date publishedAt;

    /**
     * 发布人ID
     */
    @TableField("created_by")
    private Long createdBy;

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
