package com.mysystem.futuresystemhd.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysystem.futuresystemhd.annotation.IsAdmin;
import com.mysystem.futuresystemhd.common.ErrorCode;
import com.mysystem.futuresystemhd.common.Result;
import com.mysystem.futuresystemhd.domain.ContactsGrouping;
import com.mysystem.futuresystemhd.domain.DTO.IdResultDTO;
import com.mysystem.futuresystemhd.domain.DTO.PageDTO;
import com.mysystem.futuresystemhd.domain.DTO.contacts.ContactsDTO;
import com.mysystem.futuresystemhd.domain.DTO.contacts.HandleContactsAuditingDTO;
import com.mysystem.futuresystemhd.domain.DTO.contacts.UpdateContactsDTO;
import com.mysystem.futuresystemhd.domain.VO.UserVO;
import com.mysystem.futuresystemhd.domain.VO.contacts.ContactsAuditingVO;
import com.mysystem.futuresystemhd.domain.VO.contacts.ContactsGroupingVO;
import com.mysystem.futuresystemhd.domain.VO.contacts.ContactsVO;
import com.mysystem.futuresystemhd.exception.BusinessException;
import com.mysystem.futuresystemhd.service.ContactsAuditingService;
import com.mysystem.futuresystemhd.service.ContactsGroupingService;
import com.mysystem.futuresystemhd.service.ContactsService;
import com.mysystem.futuresystemhd.service.UserService;
import io.github.classgraph.json.Id;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/contacts")
@Api(tags = "联系人相关接口")
public class ContactsController {

    @Resource
    private UserService userService;

    @Resource
    private ContactsGroupingService contactsGroupingService;

    @Resource
    private ContactsAuditingService contactsAuditingService;

    @Resource
    private ContactsService contactsService;

    /**
     * 查询全部联系人分组
     */
    @ApiOperation(value = "查询全部联系人分组")
    @GetMapping("/query/grouping/all")
    public Result<List<ContactsGroupingVO>> queryAllGrouping(){
        List<ContactsGroupingVO> groupingVOS = contactsGroupingService.selectAllGrouping();

        return Result.success(groupingVOS);
    }

    /**
     * 根据id查询分组
     * @param idResultDTO
     * @return
     */
    @ApiOperation(value = "根据id查询分组")
    @GetMapping("/query/grouping/id")
    public Result<ContactsGroupingVO> queryGroupingById(IdResultDTO idResultDTO){
        if(idResultDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        Long id = idResultDTO.getId();


        ContactsGroupingVO contactsGroupingVO = contactsGroupingService.selectGroupingById(id);

        return Result.success(contactsGroupingVO);
    }


    /**
     * 新增分组
     * @param name
     * @param request
     * @return
     */
    @ApiOperation(value = "新增分组（系统管理员）")
    @PostMapping("/save/grouping")
    public Result<Boolean> saveGrouping(String name, HttpServletRequest request){
        if(StringUtils.isBlank(name)){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        UserVO loginUser = userService.current(request);

        boolean saveResult = contactsGroupingService.insertGrouping(name,loginUser);

        return Result.success(saveResult);
    }

    /**
     * 根据id删除分组(系统管理员)
     * @param idResultDTO
     * @return
     */
    @ApiOperation("根据id删除分组(系统管理员)")
    @DeleteMapping("/delete/grouping")
    public Result<Boolean> deleteGroupingById(IdResultDTO idResultDTO){
        if(idResultDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        Long id = idResultDTO.getId();

        boolean deleteResult = contactsGroupingService.deleteById(id);

        return Result.success(deleteResult);
    }

    /**
     * 根据id修改分组(系统管理员)
     * @param contactsGroupingVO
     * @return
     */
    @ApiOperation("根据id修改分组(系统管理员)")
    @PutMapping("/modify/grouping")
    public Result<Boolean> modifyGrouping(@RequestBody ContactsGroupingVO contactsGroupingVO,HttpServletRequest request){
        if(contactsGroupingVO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        UserVO loginUser = userService.current(request);

        boolean modifyResult = contactsGroupingService.modifyById(contactsGroupingVO,loginUser);

        return Result.success(modifyResult);
    }


    /**
     * 新增联系人
     * @param contactsDTO
     * @return
     */
    @ApiOperation("新增联系人")
    @PostMapping("/save/contacts")
    public Result<Boolean> saveContacts(@RequestBody ContactsDTO contactsDTO,HttpServletRequest request){
        if(contactsDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        UserVO loginUser = userService.current(request);

        boolean insertResult = contactsService.insertContacts(contactsDTO,loginUser);

        return Result.success(insertResult);
    }

    /**
     * 修改联系人
     * @param updateContactsDTO
     * @param request
     * @return
     */
    @ApiOperation("修改联系人")
    @PutMapping("/modify/contacts")
    public Result<Boolean> modifyContacts(@RequestBody UpdateContactsDTO updateContactsDTO, HttpServletRequest request){
        if(updateContactsDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        UserVO loginUser = userService.current(request);

        boolean updateResult = contactsService.updateContacts(updateContactsDTO,loginUser);

        return Result.success(updateResult);
    }

    /**
     * 查询当前用户所有联系人
     * @param pageDTO
     * @param request
     * @return
     */
    @ApiOperation("查询当前用户所有联系人")
    @GetMapping("/query/contacts/all")
    public Result<List<ContactsVO>> queryGetByUser(PageDTO pageDTO,HttpServletRequest request){
        UserVO loginUser = userService.current(request);

        List<ContactsVO> list = contactsService.selectContactsAll(pageDTO,loginUser);

        return Result.success(list);
    }

    /**
     * 根据id查询联系人
     * @param idResultDTO
     * @return
     */
    @ApiOperation("根据id查询联系人")
    @GetMapping("/query/contacts/id")
    public Result<ContactsVO> queryById(IdResultDTO idResultDTO,HttpServletRequest request){
        if(idResultDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        Long id = idResultDTO.getId();

        UserVO loginUser = userService.current(request);
        ContactsVO contactsVO = contactsService.selectContactsById(id,loginUser);

        return Result.success(contactsVO);
    }

    /**
     * 删除联系人
     * @param idResultDTO
     * @param request
     * @return
     */
    @ApiOperation("删除联系人")
    @DeleteMapping("/delete/contacts/id")
    public Result<Boolean> deleteById(IdResultDTO idResultDTO,HttpServletRequest request){
        if(idResultDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        Long id = idResultDTO.getId();

        UserVO loginUser = userService.current(request);

        boolean deleteResult = contactsService.deleteById(id,loginUser);

        return Result.success(deleteResult);
    }


    /**
     * 处理联系人审核
     * @param handleContactsAuditingDTO
     * @return
     */
    @ApiOperation("处理联系人审核")
    @PostMapping("/handle/auditing")
    public Result<ContactsAuditingVO> handleContactsAuditing(@RequestBody HandleContactsAuditingDTO handleContactsAuditingDTO,HttpServletRequest request){
        if(handleContactsAuditingDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        UserVO loginUser = userService.current(request);

        ContactsAuditingVO contactsAuditingVO = contactsAuditingService.handleAuditing(handleContactsAuditingDTO,loginUser);

        return Result.success(contactsAuditingVO);
    }

    @ApiOperation("查询指定用户的审核")
    @GetMapping("/auditing")
    public Result<List<ContactsAuditingVO>> ContactsAuditingById(HttpServletRequest request){
        UserVO loginUser = userService.current(request);


        List<ContactsAuditingVO> list = contactsAuditingService.selectByid(loginUser);

        return Result.success(list);
    }

    /**
     * 平台管理员查看全部审核数据
     * @return
     */
    @ApiOperation("平台管理员查看全部审核数据")
    @GetMapping("/auditing/all")
    @IsAdmin
    public Result<List<ContactsAuditingVO>> ContactsAuditingByAll(PageDTO pageDTO){

        List<ContactsAuditingVO> list = contactsAuditingService.selectByAll(pageDTO);

        return Result.success(list);
    }





}
