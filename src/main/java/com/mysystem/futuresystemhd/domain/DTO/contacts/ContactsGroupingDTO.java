package com.mysystem.futuresystemhd.domain.DTO.contacts;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class ContactsGroupingDTO {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分组名称
     */
    private String name;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
