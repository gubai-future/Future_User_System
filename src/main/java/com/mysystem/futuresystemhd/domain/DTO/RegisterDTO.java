package com.mysystem.futuresystemhd.domain.DTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * 注册请求体
 */
@Data
@ApiModel(value = "RegisterDTO",description = "注册请求体")
public class RegisterDTO {



    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名",required = true)
    private String userName;

    /**
     * 用户密码
     */
    @ApiModelProperty(value = "用户密码",required = true)
    private String userPassword;

    /**
     * 确认密码
     */
    @ApiModelProperty(value = "确认密码",required = true)
    private String userCheck;

    /**
     * 邮箱
     */
    @ApiModelProperty(value = "邮箱",required = true)
    private String email;

    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号",required = true)
    private String phone;



}
