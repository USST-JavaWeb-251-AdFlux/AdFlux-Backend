package com.usst.adfluxbackend.interceptor;

import com.usst.adfluxbackend.annotation.RequireRole;
import com.usst.adfluxbackend.context.BaseContext;
import com.usst.adfluxbackend.exception.ErrorCode;
import com.usst.adfluxbackend.exception.TokenException;
import com.usst.adfluxbackend.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
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
        // 如果不是映射到方法直接通过（例如静态资源）
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // A. 尝试获取方法上的注解
        RequireRole roleAnnotation = handlerMethod.getMethodAnnotation(RequireRole.class);

        // B. 如果方法上没有注解，则尝试获取类（Controller）上的注解
        if (roleAnnotation == null) {
            // getBeanType() 获取的是 Controller 的 Class 对象
            roleAnnotation = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        // 如果注解上禁用了鉴权，直接通过
        if(roleAnnotation != null && roleAnnotation.disabled()) {
            return true;
        }

        // 从请求头中获取token
        String token = request.getHeader("Authorization");

        // 如果没有token，直接抛出异常
        if (token == null || !token.startsWith("Bearer ")) {
            throw new TokenException(ErrorCode.TOKEN_ERROR, "Token无效");
        }

        // 去掉Bearer前缀
        token = token.substring(7);
        Claims claims = null;
        try {
            // 解析token
            claims = jwtUtils.parseToken(token);
            
            // 将解析出来的用户id存入ThreadLocal
            Long userId = claims.get("userId", Long.class);
            BaseContext.setCurrentId(userId);
            

        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            // 抛出自定义异常，由全局异常处理器处理
            throw new TokenException(ErrorCode.TOKEN_ERROR, "Token无效或已过期");
        }

        // 如果无注解或注解值为空，说明只需验证用户已登录，无需验证角色，直接通过
        if (roleAnnotation == null || roleAnnotation.value().isEmpty()) {
            return true;
        }

        // 获取接口要求的角色，例如 "admin"
        String requiredRole = roleAnnotation.value();

        // 获取用户当前的角色
        String currentUserRole = claims.get("userRole", String.class);

        // 对比角色
        if (!requiredRole.equals(currentUserRole)) {
            // 角色不匹配，抛出异常或返回 false
            log.warn("用户 {} 试图访问受限接口，需要角色: {}", BaseContext.getCurrentId(), requiredRole);
            throw new TokenException(ErrorCode.NO_AUTH_ERROR, "权限不足，无法访问");
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 防止内存泄漏，确保每次请求结束后清理ThreadLocal中的数据
        BaseContext.removeCurrentId();
    }
}