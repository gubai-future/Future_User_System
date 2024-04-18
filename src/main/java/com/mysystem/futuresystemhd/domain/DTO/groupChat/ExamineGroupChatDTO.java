package com.mysystem.futuresystemhd.domain.DTO.groupChat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 审核群聊请求体
 * @TableName group_chat_examine
 */
@TableName(value ="group_chat_examine")
@Data
public class ExamineGroupChatDTO implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 审核状态(0-未审核 1-同意 2-不同意 3-过时)
     */
    private Integer status;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}