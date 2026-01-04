package com.usst.adfluxbackend.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usst.adfluxbackend.common.debug.AdDebugContext;
import com.usst.adfluxbackend.websocket.AdDebugWebSocket;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Map;

@Aspect
@Component
public class AdDebugAspect {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 拦截你的 selectAdForSlot 方法，或者拦截 Controller 层
    @Around("execution(* com.usst.adfluxbackend.service.impl.TrackerServiceImpl.selectAdForSlot(..))")
    public Object captureCalculation(ProceedingJoinPoint joinPoint) throws Throwable {

        // 1. 开启录制模式
        // (进阶建议：可以在这里判断 args，如果前端传了 debug=true 才开启，避免生产环境性能损耗)
        AdDebugContext.enable();
        long start = System.currentTimeMillis();

        try {
            // 2. 执行原有的业务逻辑
            Object result = joinPoint.proceed();
            return result;
        } finally {
            // 3. 业务执行完（无论成功失败），收集日志并推送
            if (AdDebugContext.isEnabled()) {
                Map<String, Object> finalData = AdDebugContext.getAndClearFinalData();

                // 只有当产生了数据才推送
                if (!finalData.isEmpty()) {
                    // 向数据包中追加元数据
                    finalData.put("costTime", System.currentTimeMillis() - start);

                    String jsonMsg = objectMapper.writeValueAsString(finalData);

                    // 推送到 WebSocket
                    AdDebugWebSocket.sendDebugInfo(jsonMsg);
                }
            }
        }
    }
}