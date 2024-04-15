package com.mysystem.futuresystemhd.common;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

@Data
public class DeleteVO implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 2274915950361034463L;
    /**
     * 主键
     */
    private Long id;
}
