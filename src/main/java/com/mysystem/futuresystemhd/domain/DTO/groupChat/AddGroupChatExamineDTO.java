package com.mysystem.futuresystemhd.domain.DTO.groupChat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 新增群聊审核请求体
 * @TableName group_chat_examine
 */
@TableName(value ="group_chat_examine")
@Data
public class AddGroupChatExamineDTO implements Serializable {


    /**
     * 申请人
     */
    private Long userId;

    /**
     * 群聊id
     */
    private Long groupChatId;

    /**
     * 申请留言
     */
    private String examineText;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}