package com.mysystem.futuresystemhd.information;


import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mysystem.futuresystemhd.common.ErrorCode;
import com.mysystem.futuresystemhd.configuration.GetHttpSessionConfig;
import com.mysystem.futuresystemhd.constant.WebSocketConstant;
import com.mysystem.futuresystemhd.domain.GroupChat;
import com.mysystem.futuresystemhd.domain.GroupChatMessageResult;
import com.mysystem.futuresystemhd.domain.Message;
import com.mysystem.futuresystemhd.domain.VO.UserVO;
import com.mysystem.futuresystemhd.exception.BusinessException;
import com.mysystem.futuresystemhd.service.GroupChatService;
import com.mysystem.futuresystemhd.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.servlet.http.WebConnection;
import javax.websocket.EndpointConfig;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.mysystem.futuresystemhd.constant.CookieConstant.USER_COOKIE_NAME;
import static com.mysystem.futuresystemhd.constant.WebSocketConstant.GROUP_CHAT_MESSAGE_CACHE;

@Slf4j
//跨域
@CrossOrigin
@Component
@ServerEndpoint(value = "/message/groupChat",configurator = GetHttpSessionConfig.class)
public class GroupChatMessage {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private GroupChatService groupChatService;

    @Resource
    private HttpSession httpSession;

    private Session session;

    private static ConcurrentHashMap<Long,GroupChatMessage> concurrentHashMap = new ConcurrentHashMap<>();

    @OnOpen
    public void Open(Session session, @PathParam("groupChatId")Long groupChatId,EndpointConfig config){
        httpSession = (HttpSession) config.getUserProperties().get(WebSocketConstant.WEB_SOCKET_SESSION);
        this.session = session;
        concurrentHashMap.put(groupChatId,this);
    }

    @OnMessage
    public void onMessage(String message, Session session){
        //获取发送来的消息
        Message messageRequest = JSON.parseObject(message, Message.class);


        if(httpSession == null){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }

        UserVO loginUser = (UserVO) httpSession.getAttribute(USER_COOKIE_NAME);

        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }

        Long id = loginUser.getId();

        //获取消息发送的群聊
        Long groupChatId = messageRequest.getRecipient();

        //接收的群聊是否存在
        long count = groupChatService.count(new QueryWrapper<GroupChat>().eq("id", groupChatId));

        if(count <= 0){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        //获取发送的消息
        Object message1 = messageRequest.getMessage();

        //定义消息返回
        GroupChatMessageResult groupChatMessageResult = new GroupChatMessageResult();
        groupChatMessageResult.setReceive(id);
        groupChatMessageResult.setSender(groupChatId);
        groupChatMessageResult.setMessage(message1);
        groupChatMessageResult.setSeanTime(LocalDateTime.now());

        String jsonString = JSON.toJSONString(groupChatMessageResult);

        //接收群聊存在session
        GroupChatMessage groupChatMessage = concurrentHashMap.get(groupChatId);

        //写入离线缓存
        String uuid = UUID.randomUUID().toString();

        stringRedisTemplate.opsForValue().set(GROUP_CHAT_MESSAGE_CACHE + uuid,jsonString,72, TimeUnit.HOURS);

        if(groupChatMessage != null){
            //存在session
            try {
                groupChatMessage.session.getBasicRemote().sendText(jsonString);
            } catch (IOException e) {
                log.error("发送失败",e);
                throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
            }
        }


    }

}
