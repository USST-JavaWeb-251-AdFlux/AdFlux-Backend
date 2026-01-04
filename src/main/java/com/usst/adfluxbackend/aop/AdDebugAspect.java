package com.usst.adfluxbackend.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usst.adfluxbackend.common.debug.AdDebugContext;
import com.usst.adfluxbackend.websocket.AdDebugWebSocket;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Aspect
@Component
public class AdDebugAspect {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 拦截你的 selectAdForSlot 方法，或者拦截 Controller 层
    @Around("execution(* com.usst.adfluxbackend.service.impl.TrackerServiceImpl.selectAdForSlot(..))")
    public Object captureCalculation(ProceedingJoinPoint joinPoint) throws Throwable {
        
        // 1. 开启录制模式 (可以通过判断 args 中是否包含 debug 参数来决定是否开启，这里默认开启演示)
        AdDebugContext.enable();
        long start = System.currentTimeMillis();

        try {
            // 2. 执行原有的业务逻辑
            Object result = joinPoint.proceed();
            return result;
        } finally {
            // 3. 业务执行完（无论成功失败），收集日志并推送
            if (AdDebugContext.isEnabled()) {
                List<String> logs = AdDebugContext.getAndClear();

                // 只有当产生了日志才推送
                if (!logs.isEmpty()) {
                    Map<String, Object> debugPacket = new HashMap<>();
                    debugPacket.put("timestamp", System.currentTimeMillis());
                    debugPacket.put("costTime", System.currentTimeMillis() - start);
                    debugPacket.put("steps", logs);

                    // 推送到 WebSocket
                    String jsonMsg = objectMapper.writeValueAsString(debugPacket);
                    AdDebugWebSocket.sendDebugInfo(jsonMsg);
                }
            }
        }
    }
}