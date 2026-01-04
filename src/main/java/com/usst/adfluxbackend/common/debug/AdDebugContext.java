package com.usst.adfluxbackend.common.debug;

import java.util.ArrayList;
import java.util.List;

/**
 * 线程隔离的计算过程记录上下文
 */
public class AdDebugContext {
    // 每个线程持有自己的日志列表
    private static final ThreadLocal<List<String>> LOG_HOLDER = ThreadLocal.withInitial(ArrayList::new);
    // 标记当前请求是否需要 Debug（比如只有管理员开启时才记录）
    private static final ThreadLocal<Boolean> IS_DEBUG_MODE = ThreadLocal.withInitial(() -> false);

    public static void enable() {
        IS_DEBUG_MODE.set(true);
    }

    public static boolean isEnabled() {
        return IS_DEBUG_MODE.get();
    }

    /**
     * 记录一条计算细节
     */
    public static void record(String stepName, String detail) {
        if (isEnabled()) {
            LOG_HOLDER.get().add(String.format("[%s] %s", stepName, detail));
        }
    }

    /**
     * 获取并清空日志（用于最后发送）
     */
    public static List<String> getAndClear() {
        List<String> logs = new ArrayList<>(LOG_HOLDER.get());
        LOG_HOLDER.remove();
        IS_DEBUG_MODE.remove();
        return logs;
    }
}