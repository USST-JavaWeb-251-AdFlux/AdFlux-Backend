package com.usst.adfluxbackend.interceptor;

import com.usst.adfluxbackend.context.BaseContext;
import com.usst.adfluxbackend.exception.ErrorCode;
import com.usst.adfluxbackend.exception.TokenException;
import com.usst.adfluxbackend.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头中获取token
        String token = request.getHeader("Authorization");
        
        // 如果没有token，直接抛出异常
        if (token == null || !token.startsWith("Bearer ")) {
            throw new TokenException(ErrorCode.TOKEN_ERROR, "Token无效");
        }
        
        // 去掉Bearer前缀
        token = token.substring(7);
        
        try {
            // 解析token
            Claims claims = jwtUtils.parseToken(token);
            
            // 将解析出来的用户id存入ThreadLocal
            Long userId = claims.get("userId", Long.class);
            BaseContext.setCurrentId(userId);
            
            return true;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            // 抛出自定义异常，由全局异常处理器处理
            throw new TokenException(ErrorCode.TOKEN_ERROR, "Token无效或已过期");
        }
    }
}