package com.mysystem.futuresystemhd.configuration;

import com.mysystem.futuresystemhd.common.ErrorCode;
import com.mysystem.futuresystemhd.constant.CookieConstant;
import com.mysystem.futuresystemhd.constant.WebSocketConstant;
import com.mysystem.futuresystemhd.domain.VO.UserVO;
import com.mysystem.futuresystemhd.exception.BusinessException;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

public class GetHttpSessionConfig extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        HttpSession httpSession = (HttpSession) request.getHttpSession();

        if(httpSession != null){
            synchronized(sec.getUserProperties()){
                sec.getUserProperties().put(WebSocketConstant.WEB_SOCKET_SESSION,httpSession);
            }

        }

    }
}
