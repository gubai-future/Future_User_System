package com.mysystem.futuresystemhd.information;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mysystem.futuresystemhd.common.ErrorCode;
import com.mysystem.futuresystemhd.configuration.GetHttpSessionConfig;
import com.mysystem.futuresystemhd.constant.CookieConstant;
import com.mysystem.futuresystemhd.constant.WebSocketConstant;
import com.mysystem.futuresystemhd.domain.Message;
import com.mysystem.futuresystemhd.domain.MessageResult;
import com.mysystem.futuresystemhd.domain.User;
import com.mysystem.futuresystemhd.domain.VO.UserVO;
import com.mysystem.futuresystemhd.exception.BusinessException;
import com.mysystem.futuresystemhd.service.UserService;
import com.mysystem.futuresystemhd.utils.MessageUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
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
import java.util.stream.Collectors;

import static com.mysystem.futuresystemhd.constant.CookieConstant.USER_COOKIE_NAME;

@Slf4j
@Component
//跨域
@CrossOrigin
//websocket
@ServerEndpoint(value = "/info/user",configurator = GetHttpSessionConfig.class)
@Api("用户消息发送")
public class UserMassage {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private UserService userService;

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
     * 阻塞队列
     */
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

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

        addOnlineCount();

        Long id = loginUser.getId();

        if(websocketMap.containsKey(id)){
            websocketMap.remove(id);
            websocketMap.put(id,this);
        }else{
            websocketMap.put(id,this);
        }

        endCacheMessage(id);
    }


    //发送缓存数据
    public void endCacheMessage(long userId){

        //如果消息队列中有消息则返回给用户
        List<String> messageFromQueue = getMessageFromQueue(userId);
        if(messageFromQueue == null){
            return;
        }

        //过滤拿出存在当前用消息户和系统消息的消息
        List<MessageResult> messageResults = messageFromQueue.stream()
                .map(message -> {
                    //删除消息队列里的消息
                    queue.remove(message);
                    return JSON.parseObject(message, MessageResult.class);
                }).collect(Collectors.toList());


        if(messageResults.isEmpty()){
            return;
        }

        //得到要传输的数据
        List<String> collect = messageResults.stream().filter(messageResult -> {
            Long receive = messageResult.getReceive();
            if (receive == null || userId == receive) {
                return true;
            }
            return false;
        }).map(JSON::toJSONString).collect(Collectors.toList());

        if(collect.isEmpty()){
            return;
        }

        UserMassage userMassage = websocketMap.get(userId);

        for (String message : collect) {
            try {
                userMassage.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("发送消息失败:",e);
                throw new BusinessException(ErrorCode.SEND_ERROR);
            }
        }

    }

    @Bean
    public Queue userQueue(){
        return new Queue(WebSocketConstant.WEB_SOCKET_MQ);
    }


    /**
     * 发送系统消息
     * @param messageResult
     */
    public void sendPublic(MessageResult messageResult){
        if(messageResult == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        //是否为系统消息
        Boolean isSystemMessage = messageResult.getIsSystemMessage();
        //发送方
        Long UserId = messageResult.getSender();
        //消息
        Object message = messageResult.getMessage();
        //接收方
        Long receive = messageResult.getReceive();

        String messageResponse = MessageUtils.getMessage(isSystemMessage, UserId, message,receive);

        //判断是否为系统消息
        if(isSystemMessage){
            //判断是不是公共消息
            if(UserId != null){
                //如果是推送给所有用户
                throw new BusinessException(ErrorCode.SEND_ERROR,"不是系统消息");
            }
            if(receive == null){
                //发送给全部用户
                sendAll(messageResponse);
            }else{
                //如果不是公共消息就是系统发送给指定用户的消息
                sendByUserId(messageResponse, receive);
            }
        }else {
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR,"不是公共消息");
        }
    }

    /**
     * 发送给指定用户消息
     * @param msg
     * @param userId
     */
    public void sendByUserId(String msg,Long userId){
        //判断用户是否存在
        if(userService.count(new QueryWrapper<User>().eq("id",userId)) <= 0){
            //用户不存在
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR,"用户不存在");
        }

        //发送的用户是否登录
        if(!websocketMap.containsKey(userId)){
            //如果未登录存入队列
            try {
                queue.put(msg);
            } catch (InterruptedException e) {
                log.error("存入消息失败:",e);
                throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
            }
        }

        //用户已登录
        UserMassage userMassage = websocketMap.get(userId);
        try {
            userMassage.session.getBasicRemote().sendText(msg);
        } catch (IOException e) {
            log.error("发送消息失败:",e);
            throw new BusinessException(ErrorCode.SEND_ERROR);
        }
    }


    /**
     * 推送给全部用户
     * @param msg
     */
    public void sendAll(String msg){
        //全部存入队列
        try {
            queue.put(msg);
        } catch (InterruptedException e) {
            log.error("发送给全部用户消息失败");
            throw new BusinessException(ErrorCode.SEND_ERROR);
        }
    }


    /**
     * 收到消息时调用
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message,Session session){
        Message parsed = JSON.parseObject(message, Message.class);



        Long userId = parsed.getRecipient();
        Object parsedMessage = parsed.getMessage();


        if(userService.count(new QueryWrapper<User>().eq("id",userId)) <= 0){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        UserVO loginUser = (UserVO) httpSession.getAttribute(USER_COOKIE_NAME);

        String messageMessage = MessageUtils.getMessage(false, loginUser.getId(), parsedMessage,userId);

        if(!websocketMap.containsKey(userId)){
            try {
                queue.put(message);
            } catch (InterruptedException e) {
                log.error("消息发送错误:",e);
                throw new BusinessException(ErrorCode.SEND_ERROR);
            }
        }

        UserMassage userMassage = websocketMap.get(userId);

        try {
            userMassage.session.getBasicRemote().sendText(messageMessage);
        } catch (IOException e) {
            log.error("发送消息失败:",e);
            throw new BusinessException(ErrorCode.SEND_ERROR);
        }
    }



    /**
     * 发送消息
     * @param message
     * @param loginUserId
     */
    /*public void sendMessage(String message, long loginUserId){
        Message parsed = JSON.parseObject(message, Message.class);

        Long userId = parsed.getRecipient();
        Object parsedMessage = parsed.getMessage();

        if(userService.count(new QueryWrapper<User>().eq("id",userId)) <= 0){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        String messageMessage = MessageUtils.getMessage(false, loginUserId, parsedMessage);

        if(!websocketMap.containsKey(userId)){
            try {
                queue.put(messageMessage);
            } catch (InterruptedException e) {
                log.error("消息发送错误:",e);
                throw new BusinessException(ErrorCode.SEND_ERROR);
            }
        }

        UserMassage userMassage = websocketMap.get(userId);

        try {
            userMassage.session.getBasicRemote().sendText(messageMessage);
        } catch (IOException e) {
            log.error("发送消息失败:",e);
            throw new BusinessException(ErrorCode.SEND_ERROR);
        }
    }
*/

    @OnClose
    public void onClose(){
        UserVO attribute = (UserVO) httpSession.getAttribute(USER_COOKIE_NAME);
        Long id = attribute.getId();
        websocketMap.remove(id);
        subOnlineCount();
        log.info("用户: "+ id +" 关闭链接");
    }

    /**
     * 获取消息队列指定元素的消息
     * @return
     */
    public List<String> getMessageFromQueue(Long userId){

        String loginUserId = String.valueOf(userId);

        List<String> queueList = queue.stream().filter(message -> message.contains(loginUserId) || message.contains("null")).collect(Collectors.toList());

        return queueList;
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
