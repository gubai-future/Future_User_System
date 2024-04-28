package com.mysystem.futuresystemhd.domain;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息返回封装类
 */
@Data
public class MessageResult implements Serializable {

    /**
     * 是否为系统消息
     */
    private Boolean IsSystemMessage;

    /**
     * 发送方(null为系统消息)
     */
    private Long sender;

    /**
     * 接收方(null为全部用户)
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
