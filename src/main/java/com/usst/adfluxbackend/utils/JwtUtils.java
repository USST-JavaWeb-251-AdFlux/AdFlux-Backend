package com.usst.adfluxbackend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component // 1. 必须标记为 Spring 组件
public class JwtUtils {

    // 2. 使用 @Value 读取 yml 中的值
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private Key key;

    // 3. 初始化 Key
    @PostConstruct
    public void init() {
        // 将注入的字符串转为 Key 对象
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成 Token
     */
    public String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 Token
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token) // 如果解析失败或过期，这里会抛出异常
                .getBody();
    }

    /**
     * 校验 Token
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}