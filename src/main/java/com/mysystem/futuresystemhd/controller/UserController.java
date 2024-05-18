package com.mysystem.futuresystemhd.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysystem.futuresystemhd.common.DeleteVO;
import com.mysystem.futuresystemhd.common.ErrorCode;
import com.mysystem.futuresystemhd.common.Result;
import com.mysystem.futuresystemhd.constant.CookieConstant;
import com.mysystem.futuresystemhd.constant.UserConstant;
import com.mysystem.futuresystemhd.domain.DTO.*;
import com.mysystem.futuresystemhd.domain.DTO.email.EmailDTO;
import com.mysystem.futuresystemhd.domain.DTO.email.EmailLoginCaptchaDTO;
import com.mysystem.futuresystemhd.domain.DTO.email.EmailLoginDTO;
import com.mysystem.futuresystemhd.domain.VO.UserVO;
import com.mysystem.futuresystemhd.exception.BusinessException;
import com.mysystem.futuresystemhd.service.UserService;
import com.mysystem.futuresystemhd.utils.EmailUtil;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.security.auth.login.LoginContext;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/user")
@Slf4j
@Api(tags = "用户接口")
@CrossOrigin
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private EmailUtil emailUtil;

    /**
     * 用户注册
     * @param registerDTO
     * @return
     */
    @ApiOperation(value = "注册")
    @PostMapping("/register")
    public Result<UserVO> registerUser(@RequestBody RegisterDTO registerDTO){
        if(registerDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        UserVO userVO = userService.request(registerDTO);

        if(userVO == null){
            throw new BusinessException(ErrorCode.SYSTEM_IS_ERROR,"注册失败");
        }

        return Result.success(userVO);
    }


    @ApiOperation("注册邮箱验证码")
    @PostMapping("/email/captcha/register")
    public Result<Boolean> captchaCode(@RequestBody EmailDTO emailDTO){
        if(emailDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }
        String email = emailDTO.getEmail();
        boolean emailMatcher = Pattern.compile(UserConstant.USER_EMAIL_CHECK).matcher(email).find();
        if(!emailMatcher){
            throw new BusinessException(ErrorCode.SEND_EMAIL_ERROR,"邮箱格式错误");
        }
        String emailCaptcha = emailUtil.sendRegisterEmail(email);

        if(StringUtils.isBlank(emailCaptcha)){
            return Result.success(false);
        }

        return Result.success(true);
    }

    /**
     * 登录
     * @param loginDTO
     * @return
     */
    @ApiOperation(value = "登录")
    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody LoginDTO loginDTO, HttpServletRequest request){
        if(loginDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        UserVO userVO = userService.login(loginDTO,request);

        if(userVO == null){
            throw new BusinessException(ErrorCode.LOGIN_ERROR);
        }
        return Result.success(userVO);
    }


    @ApiOperation(value = "获得当前用户")
    @GetMapping("/current")
    public Result<UserVO> getCurrent(HttpServletRequest request){

        UserVO userVO = userService.current(request);

        return Result.success(userVO);
    }

    /**
     * 删除用户
     * @param deleteVO
     * @param request
     * @return
     */
    @ApiOperation(value = "删除用户")
    @DeleteMapping("/delete")
    public Result<Boolean> deleteUser(DeleteVO deleteVO,HttpServletRequest request){
        if(deleteVO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }
        Long id = deleteVO.getId();
        if(id == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }
        boolean ResultBoo = userService.deleteById(id,request);

        return Result.success(ResultBoo);
    }


    /**
     * 退出登录
     * @param request
     * @return
     */
    @ApiOperation(value = "退出登录")
    @GetMapping("/quit")
    public Result<Boolean> QuitUser(HttpServletRequest request){
        UserVO attribute = (UserVO) request.getSession().getAttribute(CookieConstant.USER_COOKIE_NAME);
        if(attribute == null){
            return Result.success(true);
        }

        request.getSession().removeAttribute(CookieConstant.USER_COOKIE_NAME);

        return Result.success(true);
    }


    @ApiOperation(value = "修改用户")
    @PutMapping("/update")
    public Result<Boolean> updateUser(@RequestBody UserVO userVO,HttpServletRequest request){
        if(userVO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }
        UserVO loginUser = userService.current(request);
        Boolean updateResult = userService.updateUser(userVO,loginUser);

        return Result.success(updateResult);
    }




    /**
     * 修改密码
     * @param updatePasswordDTO
     * @param request
     * @return
     */
    @ApiOperation(value = "修改密码")
    @PostMapping("/update/pad")
    public Result<Boolean> UpdateUserPassword(@RequestBody UpdatePasswordDTO updatePasswordDTO,HttpServletRequest request){
        if(updatePasswordDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        boolean updateUserPadResult = userService.updateUserPad(updatePasswordDTO,request);

        return Result.success(updateUserPadResult);
    }


    /**
     * 修改身份(管理员)
     * @return
     */
    @ApiOperation(value = "管理员修改用户身份")
    @PutMapping("/set/role")
    public Result<Boolean> setRole(@RequestBody UpdateRoleDTO updateRoleDTO,HttpServletRequest request){
        if(updateRoleDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }
        UserVO loginUser = userService.current(request);

        Boolean setRoleResult = userService.updateRole(updateRoleDTO,loginUser);

        return Result.success(setRoleResult);
    }

    /**
     * 根据名字查询用户
     * @param name
     * @param pageDTO
     * @return
     */
    @ApiOperation(value = "根据名字查询用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name",value = "用户名",required = true,dataType = "String"),
    })
    @GetMapping("/query/name")
    public Result<List<UserVO>> QueryByName(String name, PageDTO pageDTO){
        if(StringUtils.isEmpty(name)){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        List<UserVO> pageUser = userService.QueryUserByName(name,pageDTO);

        return Result.success(pageUser);
    }


    /**
     * 根据标签查询用户
     * @param tags
     * @return
     */
    @ApiOperation(value = "根据标签查询用户")
    @PostMapping("/query/tags")
    public Result<List<UserVO>> getByTags(@RequestParam List<String> tags){
        if(tags == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        List<UserVO> userVOList = userService.QueryUserByTags(tags);

        return Result.success(userVOList);
    }


    /**
     * 封禁用户
     * @param id
     * @param request
     * @return
     */
    @ApiImplicitParam(name = "id",value = "用户id",required = true,dataType = "number")
    @ApiOperation("封禁用户")
    @GetMapping("/admin/close")
    public Result<Boolean> closeUser(Long id,HttpServletRequest request){
        if(id == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }


        boolean closeResult = userService.closeUser(id,request);

        return Result.success(closeResult);
    }


    @ApiOperation("查看全部用户（管理员）")
    @GetMapping("/admin/query/all")
    public Result<List<UserVO>> getUserAll(PageDTO pageDTO){

        List<UserVO> list = userService.queryAll(pageDTO);

        return Result.success(list);
    }


    /**
     * 是否为管理员(request)
     * @param request
     * @return
     */
    public Result<Boolean> isAdmin(HttpServletRequest request){

        boolean boo = userService.isAdmin(request);

        return Result.success(boo);
    }

    /**
     * 是否为管理员(userVO)
     * @param userVO
     * @return
     */
    public Result<Boolean> isAdmin(UserVO userVO){

        boolean boo = userService.isAdmin(userVO);

        return Result.success(boo);
    }

    /**
     * 邮箱登录
     * @param emailLoginDTO
     * @return
     */
    @ApiOperation("邮箱登录")
    @PostMapping("/email/login")
    public Result<UserVO> emailLogin(@RequestBody EmailLoginDTO emailLoginDTO,HttpServletRequest request){
        if(emailLoginDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        UserVO userVO = userService.emailLogin(emailLoginDTO,request);

        return Result.success(userVO);
    }

    @ApiOperation("发送邮箱登录验证码")
    @PostMapping("/email/captcha/login")
    public Result<Boolean> emailLoginCaptcha(@RequestBody EmailDTO emailDTO){
        if(emailDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        String email = emailDTO.getEmail();

        boolean emailResult = Pattern.compile(UserConstant.USER_EMAIL_CHECK).matcher(email).find();

        if(!emailResult){
            throw new BusinessException(ErrorCode.SEND_EMAIL_ERROR,"邮箱格式错误");
        }

        String emailCaptcha = emailUtil.sendLoginEmail(email);

        if(StringUtils.isBlank(emailCaptcha)){
            return Result.success(false);
        }

        return Result.success(true);

    }


    @ApiOperation("邮箱短信登录")
    @PostMapping("/email/login/captcha")
    public Result<UserVO> emailLoginEmail(@RequestBody EmailLoginCaptchaDTO emailLoginCaptchaDTO,HttpServletRequest request){
        if(emailLoginCaptchaDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        UserVO userVO = userService.emailLoginCaptcha(emailLoginCaptchaDTO,request);

        return Result.success(userVO);
    }


}
