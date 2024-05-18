package com.mysystem.futuresystemhd.domain.VO.contacts;

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
 * 联系人审核响应体
 * @TableName contacts_auditing
 */
@TableName(value ="contacts_auditing")
@Data
@ApiModel("联系人审核响应体")
public class ContactsAuditingVO implements Serializable {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 申请人
     */
    @ApiModelProperty(value = "申请人")
    private Long applicantId;

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    private Long userId;

    /**
     * 审核结果（0-待审核 1-同意 2-不同意 3-过时）
     */
    @ApiModelProperty(value = "审核结果（0-待审核 1-同意 2-不同意 3-过时）")
    private Integer auditingResult;

    /**
     * 审核意见
     */
    @ApiModelProperty(value = "审核意见")
    private String auditingView;




    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}