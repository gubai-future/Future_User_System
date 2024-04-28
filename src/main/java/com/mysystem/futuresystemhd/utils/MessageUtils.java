package com.mysystem.futuresystemhd.utils;

import com.alibaba.fastjson2.JSON;
import com.mysystem.futuresystemhd.domain.MessageResult;

import java.time.LocalDateTime;

public class MessageUtils {

    public static String getMessage(boolean isSystem,Long fromUser,Object message,Long receive){

        MessageResult messageResult = new MessageResult();

        messageResult.setIsSystemMessage(isSystem);

        if(fromUser != null){
            messageResult.setSender(fromUser);
        }

        messageResult.setSeanTime(LocalDateTime.now());
        messageResult.setMessage(message);

        String jsonString = JSON.toJSONString(messageResult);

        return jsonString;
    }

}
