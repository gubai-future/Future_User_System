package com.mysystem.futuresystemhd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mysystem.futuresystemhd.common.ErrorCode;
import com.mysystem.futuresystemhd.constant.*;
import com.mysystem.futuresystemhd.domain.DTO.*;
import com.mysystem.futuresystemhd.domain.DTO.email.EmailLoginCaptchaDTO;
import com.mysystem.futuresystemhd.domain.DTO.email.EmailLoginDTO;
import com.mysystem.futuresystemhd.domain.User;
import com.mysystem.futuresystemhd.domain.VO.UserVO;
import com.mysystem.futuresystemhd.exception.BusinessException;
import com.mysystem.futuresystemhd.mapper.UserMapper;
import com.mysystem.futuresystemhd.service.UserService;
import com.mysystem.futuresystemhd.utils.AutomaticUtil;
import com.mysystem.futuresystemhd.utils.EncryptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.mysystem.futuresystemhd.constant.UserConstant.*;


@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public UserVO request(RegisterDTO registerDTO) {
        if (registerDTO == null) {
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        String userPassword = registerDTO.getUserPassword();
        String userCheck = registerDTO.getUserCheck();
        String userName = registerDTO.getUserName();
        String email = registerDTO.getEmail();
        String phone = registerDTO.getPhone();
        //非空判断
        if (StringUtils.isAnyBlank(userPassword, userName, userCheck, email, phone)) {
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        //校验密码
        Matcher PasswordMatcher = Pattern.compile(PasswordConstant.PASSWORD_CHECK).matcher(userPassword);
        if (!PasswordMatcher.find()) {
            throw new BusinessException(ErrorCode.REGISTER_ERROR, "密码校验失败");
        }

        //确认密码和密码一致
        if (!userPassword.equals(userCheck)) {
            throw new BusinessException(ErrorCode.REGISTER_ERROR, "确认密码不一致");
        }

        //用户名
        Matcher UserNameMather = Pattern.compile(UserConstant.USER_NAME_CHECK).matcher(userName);
        if (!UserNameMather.find()) {
            throw new BusinessException(ErrorCode.REGISTER_ERROR, "用户名校验失败");
        }

        //用户名不能重复
        Long userNameCount = userMapper.selectCount(new QueryWrapper<User>().eq("user_name", userName));
        if (userNameCount >= 1) {
            throw new BusinessException(ErrorCode.REGISTER_ERROR, "用户名重复");
        }

        //邮箱格式判断
        Matcher UserEmailMatcher = Pattern.compile(UserConstant.USER_EMAIL_CHECK).matcher(email);
        if (!UserEmailMatcher.find()) {
            throw new BusinessException(ErrorCode.REGISTER_ERROR, "邮箱格式错误");
        }

        //邮箱验证码
        String emailCaptcha = registerDTO.getEmailCaptcha();

        String redisKey = EMAIL_USER_CAPTCHA_REGISTER + email;
        log.info("这是一个验证码x`"+ redisKey);
        //邮箱校验
        String emailCode = stringRedisTemplate.opsForValue().get(redisKey);

        if(StringUtils.isBlank(emailCode)){
            //如果验证码为空
            throw new BusinessException(ErrorCode.REGISTER_ERROR,"邮箱验证码为空");
        }

        //校验验证码
        String emailCaptchaCheck = emailCode.split("_")[0];

        if(!emailCaptchaCheck.equals(emailCaptcha)){
            throw new BusinessException(ErrorCode.REGISTER_ERROR,"邮箱验证码错误");
        }


        //手机号校验
        Matcher UserPhoneMatcher = Pattern.compile(UserConstant.USER_PHONE_CHECK).matcher(phone);
        if (!UserPhoneMatcher.find()) {
            throw new BusinessException(ErrorCode.REGISTER_ERROR, "手机号格式错误");
        }

        //TODO 手机号验证

        User user = new User();

        BeanUtils.copyProperties(registerDTO, user);

        //获取账户
        String account = AutomaticUtil.getAccount(this);
        if (StringUtils.isBlank(account)) {
            throw new BusinessException(ErrorCode.REGISTER_ERROR, "获取账户失败");
        }
        user.setUserAccount(account);


        //加密密码
        user.setUserPassword(EncryptionUtil.EncryptionPassword(user.getUserPassword()));

        //写入数据库
        int insert = userMapper.insert(user);

        if(insert <= 0){
            throw new BusinessException(ErrorCode.REGISTER_ERROR);
        }

        //获取脱敏数据
        UserVO desensitization = this.desensitization(user);

        stringRedisTemplate.delete(EMAIL_USER_CAPTCHA_REGISTER + email);

        return desensitization;
    }

    /**
     * 用户脱敏
     * @param user 用户
     * @return 脱敏数据
     */
    @Override
    public UserVO desensitization(User user) {
        if (user == null) {
            return null;
        }

        UserVO userVO = new UserVO();

        BeanUtils.copyProperties(user, userVO);

        return userVO;
    }

    /**
     * 登录
     * @param loginDTO 登录请求体
     * @param request request
     * @return 脱敏登录数据
     */
    @Override
    public UserVO login(LoginDTO loginDTO, HttpServletRequest request) {
        if (loginDTO == null) {
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        //校验账户
        String userAccount = loginDTO.getUserAccount();
        if (StringUtils.isBlank(userAccount)) {
            throw new BusinessException(ErrorCode.LOGIN_ERROR, "请输入账户");
        }
        Matcher AccountMatcher = Pattern.compile(AccountConstant.ACCOUNT_CHECK).matcher(userAccount);
        if (!AccountMatcher.find()) {
            throw new BusinessException(ErrorCode.LOGIN_ERROR, "账户校验失败");
        }

        //校验密码
        String userPassword = loginDTO.getUserPassword();
        if(StringUtils.isBlank(userPassword)){
            throw new BusinessException(ErrorCode.LOGIN_ERROR,"请输入密码");
        }
        Matcher PasswordMatcher = Pattern.compile(PasswordConstant.PASSWORD_CHECK).matcher(userPassword);
        if(!PasswordMatcher.find()){
            throw new BusinessException(ErrorCode.LOGIN_ERROR,"密码校验失败");
        }

        //当前用户是否存在
        Long userAccountNum = userMapper.selectCount(new QueryWrapper<User>().eq("user_account", userAccount));
        if(userAccountNum == 0){
            throw new BusinessException(ErrorCode.LOGIN_ERROR,"用户不存在");
        }

        //加密密码
        String EncyPassword = EncryptionUtil.EncryptionPassword(userPassword);

        //数据库
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("user_account", userAccount).eq("user_password", EncyPassword));

        if(user == null){
            throw new BusinessException(ErrorCode.LOGIN_ERROR,"密码错误");
        }

        if(UserConstant.USER_SEALING.equals(user.getUserRole())){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY,"被封禁");
        }

        UserVO desensitization = this.desensitization(user);

        if(request.getSession().getAttribute(CookieConstant.USER_COOKIE_NAME) != null){
            //如果存在删除session后重新设置
            request.getSession().removeAttribute(CookieConstant.USER_COOKIE_NAME);
        }
        request.getSession().setAttribute(CookieConstant.USER_COOKIE_NAME,desensitization);

        return desensitization;
    }

    /**
     * 当前用户
     * @param request request
     * @return 登录脱敏数据
     */
    @Override
    public UserVO current(HttpServletRequest request) {
        UserVO userVO = (UserVO) request.getSession().getAttribute(CookieConstant.USER_COOKIE_NAME);
        if(userVO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL,"未登录");
        }

        return userVO;
    }

    /**
     * 是否为管理员(request)
     * @param request request
     * @return 是否是管理员
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        UserVO current = this.current(request);

        Integer userRole = current.getUserRole();

        if(userRole == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        return userRole.equals(UserConstant.USER_ADMIN) || userRole.equals(UserConstant.USER_ADMIN_PLUS);
    }

    /**
     * 是否为管理员(用户)
     * @param userVO 脱敏用户数据
     * @return 是否是管理员
     */
    @Override
    public boolean isAdmin(UserVO userVO) {
        Integer userRole = userVO.getUserRole();

        if(userRole == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        return userRole.equals(UserConstant.USER_ADMIN) || userRole.equals(UserConstant.USER_ADMIN_PLUS);
    }

    /**
     * 是否为超级管理员
     * @param userVO 脱敏用户数据
     * @return 是否为超级管理员
     */
    @Override
    public boolean isAdminPlus(UserVO userVO) {
        if(userVO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        Integer userRole = userVO.getUserRole();

        if(userRole == null || !userRole.equals(UserConstant.USER_ADMIN_PLUS)){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }

        return true;
    }

    /**
     * 用户删除
     *
     * @param id 用户id
     * @param request request
     * @return 是否删除
     */
    @Override
    public boolean deleteById(Long id, HttpServletRequest request) {
        if(id == null) throw new BusinessException(ErrorCode.REQUEST_IS_NULL);

        //当前用户
        UserVO current = current(request);

        //当前用户是否存在
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", id));
        if(user == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR,"用户不存在");
        }

        //是否为当前用户或者管理员
        Long userId = current.getId();
        if((!id.equals(userId)) && (!isAdmin(current))){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }

        //超级管理员不可以删除
        long adminPlusNum = userMapper.selectCount(new QueryWrapper<User>().eq("id",id).eq("user_role",UserConstant.USER_ADMIN_PLUS));

        if(adminPlusNum >= 1){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }


         //普通管理员不可以删除其他管理员
        if(isAdmin(current)){
            //如果删除用户是普通管理员
            if(user.getUserRole().equals(UserConstant.USER_ADMIN)){
                //如果不是当前用户
                if(!Objects.equals(current.getId(), user.getId())){
                    throw new BusinessException(ErrorCode.NOT_AUTHORITY,"不可以删除其他管理员");
                }
            }
        }

        //执行删除
        int deleteResult = userMapper.deleteById(id);

        if(deleteResult <= 0){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR,"删除失败");
        }

        return true;
    }

    /**
     * 修改用户
     * @param userVO 用户脱敏数据
     * @param loginUser 登录用户
     * @return 是否修改
     */
    @Override
    public Boolean updateUser(UserVO userVO, UserVO loginUser) {
        if(userVO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        Integer userRole = userVO.getUserRole();
        //修改的权限是否为超级会员
        if(UserConstant.USER_ADMIN_PLUS.equals(userRole)){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }


        Long id = userVO.getId();
        Long userId = loginUser.getId();

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", id));

        if(user == null) {
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR,"用户不存在");
        }


        //是当前用户或者管理员
        if(!id.equals(userId) && !isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }

        //如果是管理员但不是超级管理员不可以修改其他管理员
        if(isAdmin(loginUser) && !isAdminPlus(loginUser)){
            if(!userId.equals(id) && !UserConstant.USER_ADMIN.equals(user.getUserRole())){
                throw new BusinessException(ErrorCode.NOT_AUTHORITY);
            }
        }

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateMathod(userVO, updateWrapper, id);

        int update = userMapper.update(updateWrapper);

        if(update <= 0){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR,"修改失败");
        }
        return true;
    }

    @Override
    public boolean updateUserPad(UpdatePasswordDTO updatePasswordDTO, HttpServletRequest request) {
        if(updatePasswordDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        //登录用户
        UserVO current = current(request);

        if(current == null) throw new BusinessException(ErrorCode.LOGIN_IS_NULL);

        Long userId = updatePasswordDTO.getUserId();

        if(userId == null) throw new BusinessException(ErrorCode.REQUEST_IS_NULL);

        //当前用户是否存在
        Long userNum = userMapper.selectCount(new QueryWrapper<User>().eq("id", userId));

        if(userNum <= 0){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR,"当前用户不存在");
        }

        //登录对象是当前用户还是管理员
        if(!current.getId().equals(userId) && !isAdmin(current)){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }

        //超级管理员不需要原密码
        if(!isAdminPlus(current)){

            //旧密码判断
            String oldPassword = updatePasswordDTO.getOldPassword();


             //加密
            String DigPassword = EncryptionUtil.EncryptionPassword(oldPassword);

            Long userPasswordNum = userMapper.selectCount(new QueryWrapper<User>().eq("id", userId).eq("user_password", DigPassword));

            if(userPasswordNum <= 0){
                throw new BusinessException(ErrorCode.REQUEST_IS_ERROR,"原密码错误");
            }
        }


        //新密码
        String userPassword = updatePasswordDTO.getUserPassword();
        String checkPassword = updatePasswordDTO.getCheckPassword();

        if(StringUtils.isAnyBlank(userPassword,checkPassword)){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        Matcher PasswordMatcher = Pattern.compile(PasswordConstant.PASSWORD_CHECK).matcher(userPassword);

        if(!PasswordMatcher.find()){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR,"新密码校验失败");
        }

        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR,"两次密码不一致");
        }

        //加密
        String UserPasswordDig = EncryptionUtil.EncryptionPassword(userPassword);

        int update = userMapper.update(new UpdateWrapper<User>().eq("id", userId).set("user_password", UserPasswordDig));

        if(update == 0){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR,"修改失败");
        }

        return true;
    }

    @Override
    public Boolean updateRole(UpdateRoleDTO updateRoleDTO, UserVO loginUser) {
        if(updateRoleDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        Integer userRole = updateRoleDTO.getUserRole();

        if(userRole == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        //修改为超级管理员直接报错
        if(UserConstant.USER_ADMIN_PLUS.equals(userRole)){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }

        //只能是管理员修改
        if(!isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }

        Long userId = updateRoleDTO.getUserId();

        if(userId == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        //判断用户
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", userId));

        if(user == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        Long id = loginUser.getId();

        //管理员不可以修改自己
        if(userId.equals(id)){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        //修改的对象是管理员且非超级管理员
        if(UserConstant.USER_ADMIN.equals(user.getUserRole()) && !isAdminPlus(loginUser)){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }

        //如果是修改为管理员且非超级管理员
        if(UserConstant.USER_ADMIN.equals(userRole) && !isAdminPlus(loginUser)){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }

        if(userRole.equals(user.getUserRole())){
            return true;
        }

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();

        updateWrapper.set("user_role",userRole).eq("id",userId);

        int update = userMapper.update(updateWrapper);

        if(update <= 0){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        return true;
    }

    /**
     * 根据名字查询用户
     *
     * @param name 用户名字
     * @param pageDTO 分页请求体
     * @return 用户列表
     */
    @Override
    public List<UserVO> QueryUserByName(String name, PageDTO pageDTO) {
        if(StringUtils.isEmpty(name)){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        Long current = pageDTO.getCurrent();
        Long pageSize = pageDTO.getPageSize();
        String fieldName = pageDTO.getFieldName();
        String sort = pageDTO.getSort();


        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        queryWrapper.like("user_name",name);
        queryWrapper.orderBy(StringUtils.isNotBlank(fieldName),SortConstant.SORT_ASC.equals(sort),fieldName);

        Page<User> userPage = userMapper.selectPage(new Page<>(current, pageSize), queryWrapper);

        List<User> userList = userPage.getRecords();

        return userList.stream().map(this::desensitization).collect(Collectors.toList());
    }

    @Override
    public List<UserVO> QueryUserByTags(List<String> tags) {
        if(CollectionUtils.isEmpty(tags)){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        //如果传入的标签大于5
        if(tags.size() > 5){
            //查询除去没有标签的全部用户
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.isNotNull("user_tags");
            List<User> users = userMapper.selectList(queryWrapper);

            Gson gson = new Gson();

            return users.stream().filter(user -> {
                //用户的标签
                String userTags = user.getUserTags();

                //将用户标签序序列化
                Set<String> tagsSet = gson.fromJson(userTags, new TypeToken<Set<String>>() {
                }.getType());

                //可有可无
                tagsSet = Optional.ofNullable(tagsSet).orElse(new HashSet<>());

                //如果没有查询的标签返回false
                for (String tag : tags) {
                    if(!tagsSet.contains(tag)){
                        return false;
                    }
                }

                return true;
            }).map(this::desensitization).collect(Collectors.toList());
        }

        //如果标签没有大于5
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        queryWrapper.isNotNull("user_tags");
        for (String tag : tags) {
            queryWrapper.like("user_tags",tag);
        }

        List<User> users = userMapper.selectList(queryWrapper);

        return users.stream().map(this::desensitization).collect(Collectors.toList());
    }

    @Override
    public boolean closeUser(Long id, HttpServletRequest request) {
        if(id == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        //不是管理员不是操作
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }

        UserVO loginUser = this.current(request);

        //封禁的用户是否存在
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", id));

        //当前用户是否被封禁
        if(UserConstant.USER_SEALING.equals(user.getCloseStatic())){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR,"当前用户已封禁");
        }

        //不能自己封禁自己
        if(loginUser.getId().equals(user.getId())){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        //不能封禁超级管理员
        if(UserConstant.USER_ADMIN_PLUS.equals(user.getUserRole())){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }

        //不能封禁其他管理员（超级管理员可以）
        if(UserConstant.USER_ADMIN.equals(user.getUserRole()) && !isAdminPlus(loginUser)){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();

        updateWrapper.set("close_static",UserConstant.USER_SEALING);

        int update = userMapper.update(updateWrapper);

        if(update <= 0){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR,"封禁失败");
        }

        return true;

    }

    @Override
    public List<UserVO> queryAll(PageDTO pageDTO) {
        Long current = pageDTO.getCurrent();
        Long pageSize = pageDTO.getPageSize();
        String fieldName = pageDTO.getFieldName();
        String sort = pageDTO.getSort();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        queryWrapper.orderBy(StringUtils.isNotBlank(fieldName),SortConstant.SORT_ASC.equals(sort),fieldName);

        Page<User> userPage = userMapper.selectPage(new Page<>(current, pageSize), queryWrapper);

        List<User> records = userPage.getRecords();

        return records.stream().map(this::desensitization).collect(Collectors.toList());
    }

    @Override
    public List<Long> queryAllUserId() {

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id");

        List<User> users = userMapper.selectList(queryWrapper);

        return users.stream().map(User::getId).collect(Collectors.toList());
    }

    @Override
    public UserVO emailLogin(EmailLoginDTO emailLoginDTO, HttpServletRequest request) {
        if(emailLoginDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        String email = emailLoginDTO.getEmail();

        //邮箱校验
        boolean emailMatcher = Pattern.compile(USER_EMAIL_CHECK).matcher(email).find();
        if(!emailMatcher){
            throw new BusinessException(ErrorCode.LOGIN_ERROR,"邮箱格式错误");
        }

        //该邮箱是否绑定用户
        Long emailNum = userMapper.selectCount(new QueryWrapper<User>().eq("email", email));

        if(emailNum <= 0){
            throw new BusinessException(ErrorCode.LOGIN_ERROR,"登录账号不存在");
        }

        String userPassword = emailLoginDTO.getUserPassword();

        //加密密码
        String encryptionPassword = EncryptionUtil.EncryptionPassword(userPassword);

        //校验数据库
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email", email).eq("user_password", encryptionPassword));

        //用户状态是否正常
        Integer closeStatic = user.getCloseStatic();

        if(UserConstant.USER_SEALING.equals(closeStatic)){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY,"被封禁");
        }

        //脱敏数据
        UserVO desensitization = desensitization(user);

        //设置session
        if(request.getSession().getAttribute(CookieConstant.USER_COOKIE_NAME) != null){
            //如果存在删除
            request.getSession().removeAttribute(CookieConstant.USER_COOKIE_NAME);
        }
        request.getSession().setAttribute(CookieConstant.USER_COOKIE_NAME,desensitization);

        return desensitization;
    }

    @Override
    public UserVO emailLoginCaptcha(EmailLoginCaptchaDTO emailLoginCaptchaDTO, HttpServletRequest request) {
        if(emailLoginCaptchaDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        String email = emailLoginCaptchaDTO.getEmail();

        //邮箱校验
        boolean emailMatcher = Pattern.compile(USER_EMAIL_CHECK).matcher(email).find();
        if(!emailMatcher){
            throw new BusinessException(ErrorCode.LOGIN_ERROR,"邮箱格式错误");
        }

        //该邮箱是否绑定用户
        Long emailNum = userMapper.selectCount(new QueryWrapper<User>().eq("email", email));

        if(emailNum <= 0){
            throw new BusinessException(ErrorCode.LOGIN_ERROR,"登录账号不存在");
        }

        String emailCaptcha = stringRedisTemplate.opsForValue().get(EMAIL_USER_CAPTCHA_LOGIN + email);

        if(StringUtils.isBlank(emailCaptcha)){
            //如果邮箱验证码为空
            throw new BusinessException(ErrorCode.LOGIN_ERROR);
        }

        String captchaEmail = emailLoginCaptchaDTO.getEmailCaptcha();

        emailCaptcha = emailCaptcha.split("_")[0];

        if(!Objects.equals(emailCaptcha,captchaEmail)){
            //如果验证码不匹配
            throw new BusinessException(ErrorCode.LOGIN_ERROR,"验证码错误");
        }

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email", email));

        //脱敏
        UserVO desensitization = desensitization(user);

        if(request.getSession().getAttribute(CookieConstant.USER_COOKIE_NAME) != null){
            request.getSession().removeAttribute(CookieConstant.USER_COOKIE_NAME);
        }

        request.getSession().setAttribute(CookieConstant.USER_COOKIE_NAME,desensitization);

        return desensitization;
    }


    private static void updateMathod(UserVO userVO, UpdateWrapper<User> updateWrapper, Long id) {
        String userName = userVO.getUserName();
        if(StringUtils.isNotBlank(userName)){
            updateWrapper.set("user_name",userName);
        }
        Integer userAge = userVO.getUserAge();
        if(userAge != null){
            updateWrapper.set("user_age",userAge);
        }
        Integer userSex = userVO.getUserSex();
        if(userSex != null){
            if(!userSex.equals(0) && !userSex.equals(1)){
                userSex = 2;
            }
            updateWrapper.set("user_sex",userSex);
        }
        String email = userVO.getEmail();
        Matcher EmailMatcher = Pattern.compile(UserConstant.USER_EMAIL_CHECK).matcher(email);
        if(!EmailMatcher.find()){
            email = null;
        }
        if(StringUtils.isNotBlank(email)){
            updateWrapper.set("email",email);
        }
        String phone = userVO.getPhone();
        Matcher PhoneMatcher = Pattern.compile(UserConstant.USER_PHONE_CHECK).matcher(phone);
        if(!PhoneMatcher.find()){
            phone = null;
        }
        if(StringUtils.isNotBlank(phone)){
            updateWrapper.set("phone",phone);
        }
        String userTags = userVO.getUserTags();
        if(StringUtils.isNotBlank(userTags)){
            updateWrapper.set("user_tags",userTags);
        }

        updateWrapper.eq("id", id);
    }
}
