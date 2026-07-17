package com.studyback.smartfleet.service.impl;

import com.studyback.smartfleet.service.RetryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ConcurrentModificationException;
import java.util.function.Supplier;

/**
 * 重试服务实现类
 * <p>提供自旋重试机制，捕获 {@link ConcurrentModificationException} 时自动重试</p>
 * <p>默认配置：最多 3 次重试，间隔 100ms</p>
 */
@Slf4j
@Service
public class RetryServiceImpl implements RetryService {

    /** 默认最大重试次数 */
    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    /** 默认重试间隔（毫秒） */
    private static final long DEFAULT_RETRY_INTERVAL_MS = 100;

    @Override
    public <T> T executeWithRetry(Supplier<T> supplier, int maxAttempts, long retryIntervalMs) {
        if (supplier == null) {
            throw new IllegalArgumentException("执行操作不能为空");
        }
        if (maxAttempts <= 0) {
            maxAttempts = DEFAULT_MAX_ATTEMPTS;
        }
        if (retryIntervalMs <= 0) {
            retryIntervalMs = DEFAULT_RETRY_INTERVAL_MS;
        }

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                T result = supplier.get();
                if (attempt > 1) {
                    log.info("重试成功: attempt={}/{}", attempt, maxAttempts);
                }
                return result;
            } catch (ConcurrentModificationException e) {
                lastException = e;
                log.warn("并发冲突，准备重试: attempt={}/{}, message={}",
                        attempt, maxAttempts, e.getMessage());

                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(retryIntervalMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("重试等待被中断", ie);
                        throw new ConcurrentModificationException("重试被中断: " + ie.getMessage());
                    }
                }
            }
        }

        // 所有重试均失败
        log.error("重试耗尽，操作失败: maxAttempts={}", maxAttempts, lastException);
        throw (ConcurrentModificationException) lastException;
    }
}
