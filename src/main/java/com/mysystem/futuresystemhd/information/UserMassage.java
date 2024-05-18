package com.mysystem.futuresystemhd.information;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mysystem.futuresystemhd.common.ErrorCode;
import com.mysystem.futuresystemhd.configuration.GetHttpSessionConfig;
import com.mysystem.futuresystemhd.constant.WebSocketConstant;
import com.mysystem.futuresystemhd.domain.Contacts;
import com.mysystem.futuresystemhd.domain.Message;
import com.mysystem.futuresystemhd.domain.UserMessageResult;
import com.mysystem.futuresystemhd.domain.User;
import com.mysystem.futuresystemhd.domain.VO.UserVO;
import com.mysystem.futuresystemhd.exception.BusinessException;
import com.mysystem.futuresystemhd.service.ContactsService;
import com.mysystem.futuresystemhd.service.UserService;
import com.mysystem.futuresystemhd.utils.MessageUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.mysystem.futuresystemhd.constant.CookieConstant.USER_COOKIE_NAME;
import static com.mysystem.futuresystemhd.constant.WebSocketConstant.USER_MESSAGE_CACHE;

@Slf4j
@Component
//跨域
@CrossOrigin
//websocket
@ServerEndpoint(value = "/message/user",configurator = GetHttpSessionConfig.class)
@Api("用户消息发送")
public class UserMassage {


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserService userService;

    @Resource
    private ContactsService contactsService;

    private static int onlineCount = 0;

    /**
     * 每个客户端存储对应的UserMassage对象
     */
    private static ConcurrentHashMap<Long,UserMassage> websocketMap = new ConcurrentHashMap<>();

    /**
     * 和某个客户端连接的对象，需要用它来给其他客户端发送数据
     */
    private Session session;

    /**
     * 获取当前登录用户的数据
     */
    private HttpSession httpSession;


    /**
     * 连接创建时执行
     * @param session
     * @param config
     */
    @OnOpen
    public void Open(Session session, EndpointConfig config){
        this.session = session;

        httpSession = (HttpSession) config.getUserProperties().get(WebSocketConstant.WEB_SOCKET_SESSION);

        if(httpSession == null){
            throw new BusinessException(ErrorCode.LOGIN_IS_NULL);
        }

        UserVO loginUser = (UserVO) httpSession.getAttribute(USER_COOKIE_NAME);

        if(loginUser == null){
            throw new BusinessException(ErrorCode.LOGIN_IS_NULL);
        }

        Long id = loginUser.getId();

        if(websocketMap.containsKey(id)){
            websocketMap.remove(id);
            websocketMap.put(id,this);
        }else{
            websocketMap.put(id,this);
        }

        addOnlineCount();
    }



    /**
     * 收到消息时调用
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message,Session session){
        Message parsed = JSON.parseObject(message, Message.class);

        String messageId = UUID.randomUUID().toString();

        Long userId = parsed.getRecipient();
        Object parsedMessage = parsed.getMessage();


        //发送用户是否存在
        if(userService.count(new QueryWrapper<User>().eq("id",userId)) <= 0){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        //当前登录用户
        UserVO loginUser = (UserVO) httpSession.getAttribute(USER_COOKIE_NAME);

        if(loginUser == null){
            //未登录
            throw new BusinessException(ErrorCode.LOGIN_IS_NULL);
        }

        Long loginUserId = loginUser.getId();

        //发送用户是否是当前用户联系人
        long ContactsNumber =  contactsService.count(new QueryWrapper<Contacts>().eq("user_id",loginUserId).eq("contacts_id",userId));

        if(ContactsNumber <= 0){
            //如果如果发送用户不是登录用户的联系人
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR,"不是联系人");
        }

        String messageMessage = MessageUtils.getMessage(false, loginUserId, parsedMessage,userId);

        String keyName = USER_MESSAGE_CACHE + loginUserId;

        //将消息存入redis哈希表
        stringRedisTemplate.opsForHash().put(keyName,messageId,messageMessage);

        //设置过期时间
        stringRedisTemplate.expire(keyName,72,TimeUnit.HOURS);

        if(websocketMap.containsKey(userId)){
            //如果用户在线发送消息
            UserMassage userMassage = websocketMap.get(userId);
            try {
                userMassage.session.getBasicRemote().sendText(messageMessage);
            } catch (IOException e) {
                log.error( loginUserId + " 发送给" + userId + " 的消息失败: " + e);
                throw new BusinessException(ErrorCode.SEND_ERROR);
            }
        }
    }



    @OnClose
    public void onClose(){
        UserVO attribute = (UserVO) httpSession.getAttribute(USER_COOKIE_NAME);
        Long id = attribute.getId();
        websocketMap.remove(id);
        subOnlineCount();
        log.info("用户: "+ id +" 关闭链接");
    }


    /**
     * 获取当前在线用户数
     * @return
     */
    public static synchronized int getOnlineCount(){
        return UserMassage.onlineCount;
    }


    /**
     * 当前在线用户数+1
     */
    public static synchronized void addOnlineCount(){
        UserMassage.onlineCount++;
    }

    /**
     * 当前在线用户数-1
     */
    public static synchronized void subOnlineCount(){
        UserMassage.onlineCount--;
    }




}
