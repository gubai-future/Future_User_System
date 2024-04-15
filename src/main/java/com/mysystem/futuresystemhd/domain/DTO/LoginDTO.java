package com.mysystem.futuresystemhd.domain.DTO;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求体
 */
@Data
@ApiModel(value = "LoginDTO",description = "登录请求体")
public class LoginDTO implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = -5397913069569802799L;
    /**
     * 账户
     */
    @ApiModelProperty(value = "账户",required = true)
    private String userAccount;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码",required = true)
    private String userPassword;


}
