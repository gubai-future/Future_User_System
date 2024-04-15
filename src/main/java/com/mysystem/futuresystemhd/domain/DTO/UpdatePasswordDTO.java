package com.mysystem.futuresystemhd.domain.DTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "修改密码请求体")
public class UpdatePasswordDTO implements Serializable {

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id",required = true)
    private Long userId;

    /**
     * 旧密码
     */
    @ApiModelProperty(value = "旧密码",required = true)
    private String oldPassword;

    /**
     * 新密码
     */
    @ApiModelProperty(value = "新密码",required = true)
    private String userPassword;

    /**
     * 确认密码
     */
    @ApiModelProperty(value = "确认密码",required = true)
    private String checkPassword;

}
