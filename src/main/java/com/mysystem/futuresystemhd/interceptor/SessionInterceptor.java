package com.mysystem.futuresystemhd.interceptor;

import com.mysystem.futuresystemhd.common.ErrorCode;
import com.mysystem.futuresystemhd.constant.CookieConstant;
import com.mysystem.futuresystemhd.domain.User;
import com.mysystem.futuresystemhd.domain.VO.UserVO;
import com.mysystem.futuresystemhd.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.SessionCookieConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class SessionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /*if (request.getRequestURI().contains("/swagger-ui.html")
                || request.getRequestURI().contains("/v2/api-docs")
                || request.getRequestURI().contains("/swagger-resources")
                || request.getRequestURI().contains("/doc.html")
                || request.getRequestURI().contains("/webjars/**")) {
            return true;
        }*/

        UserVO user = (UserVO) request.getSession().getAttribute(CookieConstant.USER_COOKIE_NAME);

        if(user == null){
            return false;
        }

        Integer fj = 1;

        if(fj.equals(user.getCloseStatic())){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY,"被封禁");
        }

        return true;
    }

}
