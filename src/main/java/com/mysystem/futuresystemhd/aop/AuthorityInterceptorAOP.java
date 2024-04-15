package com.mysystem.futuresystemhd.aop;


import com.mysystem.futuresystemhd.annotation.Authority;
import com.mysystem.futuresystemhd.common.ErrorCode;
import com.mysystem.futuresystemhd.constant.AuthorityConstant;
import com.mysystem.futuresystemhd.domain.VO.UserVO;
import com.mysystem.futuresystemhd.exception.BusinessException;
import com.mysystem.futuresystemhd.service.UserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
public class AuthorityInterceptorAOP {

    @Resource
    private UserService userService;

    @Around("@annotation(authority)")
    public Object odInterceptor(ProceedingJoinPoint proceedingJoinPoint, Authority authority) throws Throwable {
        String userAuthority = authority.authority();

        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();

        UserVO userVO = userService.current(request);

        Integer closeStatic = userVO.getCloseStatic();

        //如果被封禁
        if(AuthorityConstant.getByStatusId(closeStatic).equals(AuthorityConstant.STATUS_BAN)){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }

        Integer userRole = userVO.getUserRole();
        String byStatusId = AuthorityConstant.getByStatusId(userRole);
        //如果是普通用户
        if(byStatusId.equals(AuthorityConstant.USER_ORDINARY) || byStatusId.equals(AuthorityConstant.USER_ORDINARY)){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }

        if(!byStatusId.equals(userAuthority)){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }

        return proceedingJoinPoint.proceed();


    }


}
