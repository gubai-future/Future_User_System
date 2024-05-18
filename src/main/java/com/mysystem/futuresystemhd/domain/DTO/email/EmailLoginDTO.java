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
@ApiModel(value = "邮箱登录请求体")
public class EmailLoginDTO implements Serializable {

    /**
     * 邮箱
     */
    @ApiModelProperty(value = "邮箱",required = true)
    private String email;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码",required = true)
    private String userPassword;

}
