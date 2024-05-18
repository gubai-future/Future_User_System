package com.mysystem.futuresystemhd.domain;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群聊消息返回封装类
 */
@Data
public class GroupChatMessageResult implements Serializable {


    /**
     * 发送方(用户)
     */
    private Long sender;

    /**
     * 接收方(群聊)
     */
    private Long receive;

    /**
     * 发送的消息
     */
    private Object message;

    /**
     * 发送时间
     */
    private LocalDateTime seanTime;
}
