package com.usst.adfluxbackend.common.debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 线程隔离的计算过程记录上下文
 * 升级版：支持存储结构化对象
 */
public class AdDebugContext {
    // 1. 存纯文本日志 (List<String>)
    private static final ThreadLocal<List<String>> LOG_HOLDER = ThreadLocal.withInitial(ArrayList::new);

    // 2. 存结构化数据 (Map<String, Object>) - 这里的 Object 不会被转成 String
    private static final ThreadLocal<Map<String, Object>> DATA_HOLDER = ThreadLocal.withInitial(ConcurrentHashMap::new);

    // 3. Debug 开关
    private static final ThreadLocal<Boolean> IS_DEBUG_MODE = ThreadLocal.withInitial(() -> false);

    public static void enable() {
        IS_DEBUG_MODE.set(true);
    }

    public static boolean isEnabled() {
        return IS_DEBUG_MODE.get();
    }

    /**
     * 记录结构化数据对象
     * 前端接收时会直接得到 JSON 对象
     * @param key  前端取值用的字段名，如 "budgetDetails"
     * @param data 具体的对象 (List, Map, JSONObject, POJO 等)
     */
    public static void recordData(String key, Object data) {
        if (isEnabled()) {
            DATA_HOLDER.get().put(key, data);
        }
    }

    /**
     * 返回给 AOP 层直接序列化
     */
    public static Map<String, Object> getAndClearFinalData() {
        Map<String, Object> finalResult = new HashMap<>();

        // 1. 取出文本日志，放入 "logs" 字段
        List<String> logs = LOG_HOLDER.get();
        if (!logs.isEmpty()) {
            finalResult.put("logs", new ArrayList<>(logs));
        }

        // 2. 取出结构化数据，直接平铺合并到结果中
        // 例如：finalResult 中会有 "budgetDetails": [...]
        finalResult.putAll(DATA_HOLDER.get());

        // 3. 清理 ThreadLocal，防止内存泄漏
        LOG_HOLDER.remove();
        DATA_HOLDER.remove();
        IS_DEBUG_MODE.remove();

        return finalResult;
    }
}