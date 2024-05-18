package com.mysystem.futuresystemhd.utils;

import com.alibaba.fastjson2.JSON;
import com.mysystem.futuresystemhd.domain.UserMessageResult;

import java.time.LocalDateTime;

public class MessageUtils {

    public static String getMessage(boolean isSystem,Long fromUser,Object message,Long receive){

        UserMessageResult userMessageResult = new UserMessageResult();

        //是否是系统消息
        userMessageResult.setIsSystemMessage(isSystem);

        if(fromUser != null){
            userMessageResult.setSender(fromUser);
        }

        //消息接收者
        userMessageResult.setReceive(receive);

        //当前时间
        userMessageResult.setSeanTime(LocalDateTime.now());
        //消息
        userMessageResult.setMessage(message);

        String jsonString = JSON.toJSONString(userMessageResult);

        return jsonString;
    }

}
