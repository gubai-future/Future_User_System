package com.mysystem.futuresystemhd.domain.DTO.groupChat;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 群聊新增请求体
 * @TableName groupChat
 */
@TableName(value ="group_chat")
@Data
@ApiModel("群聊新增请求体")
public class AddGroupChatDTO implements Serializable {

    /**
     * 群聊名
     */
    @ApiModelProperty(value = "群聊名",required = true)
    private String name;


    /**
     * 群聊介绍
     */
    @ApiModelProperty(value = "群聊介绍")
    private String groupChatTxt;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}