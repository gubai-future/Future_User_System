package com.mysystem.futuresystemhd.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 群聊表
 * @TableName groupChat
 */
@TableName(value ="group_chat")
@Data
public class GroupChat implements Serializable {
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
     * 群主
     */
    private Long userId;

    /**
     * 群聊介绍
     */
    private String groupChatTxt;

    /**
     * 最大人数
     */
    private Integer maxPeopleNum;

    /**
     * 当前人数
     */
    private Integer currentPeopleNum;

    /**
     * 群聊状态 (0-正常 1-封禁)
     */
    private Integer status;

    /**
     * 创建人
     */
    private Long createId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改人
     */
    private Long updateId;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 是否删除(0-未删除 1-删除)
     */
    private Integer isDelete;

    /**
     * 加入设置(0-公开 1-需同意)
     */
    private Integer disclosure;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}