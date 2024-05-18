package com.mysystem.futuresystemhd.domain.DTO;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("id请求体")
public class IdResultDTO implements Serializable {


    @TableField(exist = false)
    private static final long serialVersionUID = 5917336557814304023L;


    @ApiModelProperty(value = "id",required = true)
    private Long id;

}
