package com.mysystem.futuresystemhd.aop;


import com.mysystem.futuresystemhd.annotation.IsAdmin;
import com.mysystem.futuresystemhd.common.ErrorCode;
import com.mysystem.futuresystemhd.constant.CookieConstant;
import com.mysystem.futuresystemhd.domain.VO.UserVO;
import com.mysystem.futuresystemhd.exception.BusinessException;
import com.mysystem.futuresystemhd.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.security.auth.login.LoginContext;
import javax.servlet.http.HttpServletRequest;

@Aspect
public class IsAdminAOP {

    @Autowired
    private UserService userService;


    @Around("@annotation(isAdmin)")
    public void IsUserAdmin(ProceedingJoinPoint proceedingJoinPoint, IsAdmin isAdmin){
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();

        if(request == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        UserVO loginUser = (UserVO) request.getSession().getAttribute(CookieConstant.USER_COOKIE_NAME);

        if(loginUser == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        boolean admin = userService.isAdmin(loginUser);

        if(!admin){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }

    }

}
