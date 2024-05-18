package com.mysystem.futuresystemhd.job;

import com.alibaba.fastjson2.JSONObject;
import com.mysystem.futuresystemhd.common.ErrorCode;
import com.mysystem.futuresystemhd.domain.GroupChatMessageResult;
import com.mysystem.futuresystemhd.domain.MessageCache;
import com.mysystem.futuresystemhd.domain.UserMessageResult;
import com.mysystem.futuresystemhd.exception.BusinessException;
import com.mysystem.futuresystemhd.service.MessageCacheService;
import com.mysystem.futuresystemhd.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.mysystem.futuresystemhd.constant.WebSocketConstant.USER_CACHE_TIMING;
import static com.mysystem.futuresystemhd.constant.WebSocketConstant.USER_MESSAGE_CACHE;

@Component
@Slf4j
public class endMessageCache {

    @Resource
    private MessageCacheService messageCacheService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Scheduled(cron = "59 58 23 * * *")
    public void timingMessageCache(){
        RLock lock = redissonClient.getLock(USER_CACHE_TIMING);

        try {
            if(lock.tryLock(0,-1, TimeUnit.SECONDS)){

                List<Long> longs = userService.queryAllUserId();

                for (Long userId : longs) {
                    String keyName = USER_MESSAGE_CACHE + userId;

                    HashOperations<String, Object, Object> opsForHash = stringRedisTemplate.opsForHash();
                    Map<Object, Object> entries = opsForHash.entries(keyName);

                    for (Map.Entry<Object, Object> userMessage : entries.entrySet()) {
                        String message = (String)userMessage.getValue();
                        UserMessageResult userMessageResult = JSONObject.parseObject(message, UserMessageResult.class);

                        //写入数据库
                        MessageCache messageCache = new MessageCache();
                        messageCache.setMessage((String) userMessageResult.getMessage());
                        messageCache.setCreateDatetime(userMessageResult.getSeanTime());
                        messageCache.setStarterId(userMessageResult.getSender());
                        messageCache.setReceiverId(userMessageResult.getReceive());

                        boolean save = messageCacheService.save(messageCache);

                        if(!save){
                            //添加消息失败
                            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("缓存消息写入数据库失败:",e);
        }finally {
            lock.unlock();
        }
    }

}
