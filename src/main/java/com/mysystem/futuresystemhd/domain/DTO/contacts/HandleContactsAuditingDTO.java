package com.mysystem.futuresystemhd.domain.DTO.contacts;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 处理联系人审核请求体
 * @TableName contacts_auditing
 */
@ApiModel("处理联系人审核请求体")
@TableName(value ="contacts_auditing")
@Data
public class HandleContactsAuditingDTO implements Serializable {
    /**
     * id
     */
    @ApiModelProperty(value = "id",required = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 请求用户
     */
    @ApiModelProperty(value = "请求用户",required = true)
    private Long userId;

    /**
     * 审核结果（0-待审核 1-同意 2-不同意 3-过时）
     */
    @ApiModelProperty(value = "审核结果（0-待审核 1-同意 2-不同意 3-过时）",required = true)
    private Integer auditingResult;

    /**
     * 审核意见
     */
    @ApiModelProperty(value = "审核意见")
    private String auditingView;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}