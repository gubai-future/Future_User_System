package com.mysystem.futuresystemhd.controller;

import com.mysystem.futuresystemhd.common.ErrorCode;
import com.mysystem.futuresystemhd.common.Result;
import com.mysystem.futuresystemhd.domain.DTO.groupChat.AddGroupChatDTO;
import com.mysystem.futuresystemhd.domain.VO.UserVO;
import com.mysystem.futuresystemhd.exception.BusinessException;
import com.mysystem.futuresystemhd.service.GroupChatService;
import com.mysystem.futuresystemhd.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/GroupChat")
public class GroupChatController {

    @Resource
    private GroupChatService groupChatService;

    @Resource
    private UserService userService;


    @PostMapping("/add")
    public Result<Boolean> addGroupChat(@RequestBody AddGroupChatDTO addGroupChatDTO, HttpServletRequest request){
        if(addGroupChatDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        UserVO loginUser = userService.current(request);

        boolean addResult = groupChatService.insertgroupChat(addGroupChatDTO,loginUser);

        return Result.success(addResult);
    }





}
