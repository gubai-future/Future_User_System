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
 * 联系人响应体
 * @TableName contacts
 */
@TableName(value ="contacts")
@Data
@ApiModel("联系人响应体")
public class ContactsVO implements Serializable {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    private Long userId;

    /**
     * 联系人id
     */
    @ApiModelProperty(value = "联系人id")
    private Long contactsId;

    /**
     * 备注名字
     */
    @ApiModelProperty(value = "备注名字")
    private String remarksName;

    /**
     * 联系人身份
     */
    @ApiModelProperty(value = "联系人身份")
    private String contactsIdentity;

    /**
     * 分组
     */
    @ApiModelProperty(value = "分组")
    private Integer groupingId;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}