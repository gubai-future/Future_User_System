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
 * 联系人请求体
 * @TableName contacts
 */
@TableName(value ="contacts")
@Data
@ApiModel("联系人请求体")
public class ContactsDTO implements Serializable {



    /**
     * 联系人id
     */
    @ApiModelProperty(value = "联系人id",required = true)
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
     * 分组(默认0)
     */
    @ApiModelProperty(value = "")
    private Integer groupingId = 0;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}