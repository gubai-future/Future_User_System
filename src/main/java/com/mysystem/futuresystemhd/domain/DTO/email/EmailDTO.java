package com.mysystem.futuresystemhd.domain.DTO.email;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("邮箱请求体")
public class EmailDTO {

    @ApiModelProperty(value = "邮箱",required = true)
    private String email;
}
