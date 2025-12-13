package com.usst.adfluxbackend.config;

import com.usst.adfluxbackend.interceptor.JwtTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private JwtTokenInterceptor jwtTokenInterceptor;

    @Value("${ad-system.upload-path}")
    private String uploadPath;

    @Value("${ad-system.access-prefix}")
    private String accessPrefix;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 计算资源映射的排除路径
        // 确保 accessPrefix 是以 "/" 结尾的，这样拼接 "**"
        String resourceExclusion = accessPrefix.endsWith("/") ? accessPrefix + "**" : accessPrefix + "/**";

        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/register",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        resourceExclusion
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 2. 处理 URL 匹配模式
        String pathPattern = accessPrefix.endsWith("/") ? accessPrefix + "**" : accessPrefix + "/**";

        // 3. 处理本地磁盘路径 (自动补全 file: 和 /)
        String localPath = uploadPath;
        // 如果不是 file: 开头，加上它
        if (!localPath.startsWith("file:")) {
            localPath = "file:" + localPath;
        }
        // 如果末尾没有斜杠，加上它
        if (!localPath.endsWith("/") && !localPath.endsWith(File.separator)) {
            localPath = localPath + "/";
        }

        registry.addResourceHandler(pathPattern)
                .addResourceLocations(localPath);
    }
}