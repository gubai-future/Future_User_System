package com.mysystem.futuresystemhd.domain.DTO.groupChat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改群聊请求体
 * @TableName groupChat
 */
@TableName(value ="group_chat")
@Data
public class UpdateGroupChatDTO implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 群聊名
     */
    private String name;

    /**
     * 群聊介绍
     */
    private String groupChatTxt;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}