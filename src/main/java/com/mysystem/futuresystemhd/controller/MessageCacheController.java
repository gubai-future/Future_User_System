package com.mysystem.futuresystemhd.controller;

import com.mysystem.futuresystemhd.common.ErrorCode;
import com.mysystem.futuresystemhd.common.Result;
import com.mysystem.futuresystemhd.domain.DTO.messageCache.MessageCacheUserByContactsIdDTO;
import com.mysystem.futuresystemhd.domain.DTO.messageCache.MessageCacheUserDatetimeDTO;
import com.mysystem.futuresystemhd.domain.MessageCache;
import com.mysystem.futuresystemhd.domain.VO.UserVO;
import com.mysystem.futuresystemhd.exception.BusinessException;
import com.mysystem.futuresystemhd.service.MessageCacheService;
import com.mysystem.futuresystemhd.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/messageCache")
@Api(tags = "消息缓存相关接口")
public class MessageCacheController {

    @Resource
    private MessageCacheService messageCacheService;

    @Resource
    private UserService userService;

    @ApiOperation("返回指定用户的消息缓存")
    @ApiImplicitParam(value = "联系人id",name = "contactsId",required = true)
    @PostMapping("/current/message")
    public Result<List<String>> currentDayMessage(@RequestBody MessageCacheUserByContactsIdDTO messageCacheUserByContactsIdDTO, HttpServletRequest request){
        if(messageCacheUserByContactsIdDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        UserVO loginUser = userService.current(request);


        List<String> messageCache = messageCacheService.currentDayMessageCache(messageCacheUserByContactsIdDTO,loginUser);

        return Result.success(messageCache);
    }


    @ApiOperation("根据日期返回指定用户消息缓存")
    @PostMapping("/message/by/datetime")
    public Result<List<MessageCache>> MessageByDateTime(@RequestBody MessageCacheUserDatetimeDTO messageCacheUserDatetimeDTO,HttpServletRequest request){
        if(messageCacheUserDatetimeDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        UserVO loginUser = userService.current(request);

        List<MessageCache> messageList = messageCacheService.getByDatetimeMessage(messageCacheUserDatetimeDTO,loginUser);

        return Result.success(messageList);
    }



}
