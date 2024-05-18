package com.mysystem.futuresystemhd.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName user_groupChat_info
 */
@TableName(value ="user_group_chat_info")
@Data
public class UserGroupChatInfo implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 群聊id
     */
    private Long groupChatId;

    /**
     * 群聊用户名
     */
    private String groupChatName;

    /**
     * 权限(0-普通 1-管理员 2-群主)
     */
    private Integer authority;

    /**
     * 状态(0-正常 1-禁言)
     */
    private Integer status;

    /**
     * 加入时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 是否删除(0-未删除 1-删除)
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}