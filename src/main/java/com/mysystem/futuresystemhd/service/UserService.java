package com.mysystem.futuresystemhd.service;

import com.mysystem.futuresystemhd.domain.DTO.*;
import com.mysystem.futuresystemhd.domain.DTO.email.EmailLoginCaptchaDTO;
import com.mysystem.futuresystemhd.domain.DTO.email.EmailLoginDTO;
import com.mysystem.futuresystemhd.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mysystem.futuresystemhd.domain.VO.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param registerDTO
     * @return
     */
    UserVO request(RegisterDTO registerDTO);

    /**
     * 用户脱敏
     * @param user
     * @return
     */
    UserVO desensitization(User user);


    /**
     * 登录
     *
     * @param loginDTO
     * @param request
     * @return
     */
    UserVO login(LoginDTO loginDTO, HttpServletRequest request);

    /**
     * 获得当前用户
     * @param request
     * @return
     */
    UserVO current(HttpServletRequest request);

    /**
     * 是否为管理员(request)
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员(userVO)
     * @param userVO
     * @return
     */
    boolean isAdmin(UserVO userVO);


    /**
     * 是否为超级管理员
     * @param userVO
     * @return
     */
    boolean isAdminPlus(UserVO userVO);

    /**
     * 用户删除
     *
     * @param id
     * @param request
     * @return
     */
    boolean deleteById(Long id, HttpServletRequest request);

    /**
     * 修改用户
     * @param userVO
     * @param loginUser
     * @return
     */
    Boolean updateUser(UserVO userVO, UserVO loginUser);

    /**
     * 修改密码
     * @param updatePasswordDTO
     * @param request
     * @return
     */
    boolean updateUserPad(UpdatePasswordDTO updatePasswordDTO, HttpServletRequest request);

    /**
     * 管理员修改用户身份
     * @param updateRoleDTO
     * @param loginUser
     * @return
     */
    Boolean updateRole(UpdateRoleDTO updateRoleDTO, UserVO loginUser);

    /**
     * 根据名字查询用户
     *
     * @param name
     * @param pageDTO
     * @return
     */
    List<UserVO> QueryUserByName(String name, PageDTO pageDTO);

    /**
     * 根据标签查询用户
     * @param tags
     * @return
     */
    List<UserVO> QueryUserByTags(List<String> tags);

    /**
     * 封禁用户
     * @param id
     * @param request
     * @return
     */
    boolean closeUser(Long id, HttpServletRequest request);

    /**
     * 管理员查看全部用户
     * @param pageDTO
     * @return
     */
    List<UserVO> queryAll(PageDTO pageDTO);

    /**
     * 获取全部用户id
     * @return
     */
    List<Long> queryAllUserId();

    /**
     * 邮箱登录
     *
     * @param emailLoginDTO
     * @param request
     * @return
     */
    UserVO emailLogin(EmailLoginDTO emailLoginDTO, HttpServletRequest request);

    /**
     * 邮箱短信登录
     * @param emailLoginCaptchaDTO
     * @param request
     * @return
     */
    UserVO emailLoginCaptcha(EmailLoginCaptchaDTO emailLoginCaptchaDTO, HttpServletRequest request);
}
