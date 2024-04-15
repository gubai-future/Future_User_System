package com.mysystem.futuresystemhd.domain.DTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "修改权限请求体")
public class UpdateRoleDTO implements Serializable {

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id",required = true,dataType = "Long")
    private Long userId;

    /**
     * 用户身份
     */
    @ApiModelProperty(value = "用户身份 0-普通用户 1-会员 2-管理员",required = true,dataType = "Integer")
    private Integer userRole;

}
