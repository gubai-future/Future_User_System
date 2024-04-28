package com.mysystem.futuresystemhd.domain;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("消息返回封装类")
public class Message implements Serializable {


    private static final long serialVersionUID = 3331152460405596444L;

    /**
     * 收消息方(用户或者群聊)
     */
    private Long recipient;

    /**
     * 发送的消息
     */
    private Object message;

}
