package com.mysystem.futuresystemhd.controller;

import com.mysystem.futuresystemhd.common.ErrorCode;
import com.mysystem.futuresystemhd.common.Result;
import com.mysystem.futuresystemhd.domain.DTO.groupChat.AddGroupChatDTO;
import com.mysystem.futuresystemhd.domain.DTO.groupChat.AddGroupChatExamineDTO;
import com.mysystem.futuresystemhd.domain.DTO.groupChat.ExamineGroupChatDTO;
import com.mysystem.futuresystemhd.domain.DTO.groupChat.UpdateGroupChatDTO;
import com.mysystem.futuresystemhd.domain.GroupChatExamine;
import com.mysystem.futuresystemhd.domain.User;
import com.mysystem.futuresystemhd.domain.VO.GroupChatExamineVO;
import com.mysystem.futuresystemhd.domain.VO.UserVO;
import com.mysystem.futuresystemhd.exception.BusinessException;
import com.mysystem.futuresystemhd.service.GroupChatService;
import com.mysystem.futuresystemhd.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/GroupChat")
@Api(tags = "群聊相关接口")
public class GroupChatController {

    @Resource
    private GroupChatService groupChatService;

    @Resource
    private UserService userService;


    /**
     * 创建群聊
     * @param addGroupChatDTO
     * @param request
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "创建群聊")
    public Result<Boolean> addGroupChat(@RequestBody AddGroupChatDTO addGroupChatDTO, HttpServletRequest request){
        if(addGroupChatDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        UserVO loginUser = userService.current(request);

        boolean addResult = groupChatService.insertGroupChat(addGroupChatDTO,loginUser);

        return Result.success(addResult);
    }


    /**
     * 修改群聊
     * @param updateGroupChatDTO
     * @param request
     * @return
     */
    @PutMapping("/update")
    @ApiOperation(value = "修改群聊")
    public Result<Boolean> updateGroupChat(@RequestBody UpdateGroupChatDTO updateGroupChatDTO,HttpServletRequest request){
        if(updateGroupChatDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        UserVO loginUser = userService.current(request);

        boolean updateResult = groupChatService.updateGroupChat(updateGroupChatDTO,loginUser);

        return Result.success(updateResult);
    }


    /**
     * 新增群聊审核
     * @param addGroupChatExamineDTO
     * @return
     */
    @ApiOperation("群聊审核")
    @PostMapping("/add/examine")
    public Result<GroupChatExamineVO> addExamine(@RequestBody AddGroupChatExamineDTO addGroupChatExamineDTO){
        if(addGroupChatExamineDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        GroupChatExamineVO examineResult = groupChatService.addExamine(addGroupChatExamineDTO);

        return Result.success(examineResult);
    }


    /**
     * 审核群聊
     * @param examineGroupChatDTO
     * @return
     */
    @ApiOperation("审核群聊")
    @PostMapping("/examine")
    public Result<GroupChatExamineVO> examineGroupChat(@RequestBody ExamineGroupChatDTO examineGroupChatDTO,HttpServletRequest request){
        if(examineGroupChatDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        UserVO loginUser = userService.current(request);

        GroupChatExamineVO groupChatExamineResult = groupChatService.examineGroupChat(examineGroupChatDTO,loginUser);

        return Result.success(groupChatExamineResult);
    }


}
