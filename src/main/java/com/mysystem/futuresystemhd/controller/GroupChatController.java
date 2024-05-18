package com.mysystem.futuresystemhd.controller;

import com.mysystem.futuresystemhd.common.ErrorCode;
import com.mysystem.futuresystemhd.common.Result;
import com.mysystem.futuresystemhd.domain.DTO.groupChat.*;
import com.mysystem.futuresystemhd.domain.GroupChat;
import com.mysystem.futuresystemhd.domain.GroupChatExamine;
import com.mysystem.futuresystemhd.domain.User;
import com.mysystem.futuresystemhd.domain.VO.GroupChatExamineVO;
import com.mysystem.futuresystemhd.domain.VO.GroupChatVO;
import com.mysystem.futuresystemhd.domain.VO.UserVO;
import com.mysystem.futuresystemhd.exception.BusinessException;
import com.mysystem.futuresystemhd.service.GroupChatService;
import com.mysystem.futuresystemhd.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/GroupChat")
@Api(tags = "群聊相关接口")
@CrossOrigin
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

    @ApiOperation("加入群聊")
    @PostMapping("join")
    public Result<Boolean> joinGroupChat(@RequestBody JoinGroupChatDTO joinGroupChatDTO, HttpServletRequest request){
        if(joinGroupChatDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        Long groupChatId = joinGroupChatDTO.getId();

        UserVO loginUser = userService.current(request);

        boolean joinResult = groupChatService.joinGroupChat(groupChatId,loginUser);

        if(!joinResult){
            return Result.success(false,"请等待管理员同意");
        }

        return Result.success(true);
    }


    /**
     * 退出群聊
     * @param quitGroupChatDTO
     * @param request
     * @return
     */
    @ApiOperation("退出群聊")
    @PostMapping("/quit")
    public Result<Boolean> quitGroupChat(@RequestBody QuitGroupChatDTO quitGroupChatDTO,HttpServletRequest request){
        if(quitGroupChatDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        UserVO loginUser = userService.current(request);

        boolean quitResult = groupChatService.QuitGroupChat(quitGroupChatDTO,loginUser);

        return Result.success(quitResult);
    }

    /**
     * 根据名字查询群聊
     * @param queryGroupChatByNameDTO
     * @return
     */
    @ApiOperation("根据名字查看群聊")
    @GetMapping("/query/name")
    public Result<List<GroupChatVO>> QueryGroupChatByName(QueryGroupChatByNameDTO queryGroupChatByNameDTO){
        if(queryGroupChatByNameDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        List<GroupChatVO> groupChatVOList = groupChatService.selectByName(queryGroupChatByNameDTO);

        return Result.success(groupChatVOList);
    }

    @ApiOperation("管理员查看群聊")
    @GetMapping("/admin/query")
    public Result<List<GroupChat>> AdminQueryGroupChat(QueryGroupChatDTO queryGroupChatDTO,HttpServletRequest request){

        UserVO loginUser = userService.current(request);

        List<GroupChat> groupChatList = groupChatService.selectAdminBy(queryGroupChatDTO,loginUser);

        return Result.success(groupChatList);
    }

    /**
     * 设置用户群聊名字
     * @param name
     * @param GroupChatId
     * @param request
     * @return
     */
    @ApiOperation("设置用户群聊名字")
    @GetMapping("/install/name")
    public Result<Boolean> SetGroupChatName(String name,Long GroupChatId,HttpServletRequest request){
        if(StringUtils.isBlank(name) || GroupChatId == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        UserVO loginUser = userService.current(request);

        boolean installResult = groupChatService.installGroupChatName(name,GroupChatId,loginUser);

        return Result.success(installResult);
    }


    @ApiOperation("群主修改用户身份")
    @PostMapping("/install/authority")
    public Result<Boolean> SetGroupChatAuthority(@RequestBody InstallGroupChatAuthorityDTO installGroupChatAuthorityDTO,HttpServletRequest request){
        if(installGroupChatAuthorityDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        UserVO loginUser = userService.current(request);

        boolean installResult = groupChatService.installGroupChatAthority(installGroupChatAuthorityDTO,loginUser);

        return Result.success(installResult);
    }



}
