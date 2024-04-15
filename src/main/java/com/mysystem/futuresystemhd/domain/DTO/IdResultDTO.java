package com.mysystem.futuresystemhd.domain.DTO;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

@Data
public class IdResultDTO implements Serializable {


    @TableField(exist = false)
    private static final long serialVersionUID = 5917336557814304023L;


    private Long id;

}
