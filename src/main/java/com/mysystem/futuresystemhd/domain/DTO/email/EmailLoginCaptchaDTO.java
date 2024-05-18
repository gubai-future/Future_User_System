package com.mysystem.futuresystemhd.domain.DTO.email;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "邮箱验证码登录请求体")
public class EmailLoginCaptchaDTO implements Serializable {

    @ApiModelProperty(value = "邮箱",required = true)
    private String email;

    @ApiModelProperty(value = "邮箱验证码",required = true)
    private String emailCaptcha;

}
