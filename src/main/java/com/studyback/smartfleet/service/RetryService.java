package com.studyback.smartfleet.service;

import java.util.function.Supplier;

/**
 * 重试服务接口
 * <p>提供自旋重试机制，用于乐观锁冲突等并发场景</p>
 */
public interface RetryService {

    /**
     * 带重试的执行方法
     * <p>当操作抛出 {@link java.util.concurrent.ConcurrentModificationException} 时自动重试</p>
     *
     * @param supplier         要执行的操作
     * @param maxAttempts      最大重试次数
     * @param retryIntervalMs  重试间隔（毫秒）
     * @param <T>              返回值类型
     * @return 操作结果
     * @throws RuntimeException 最后一次重试仍失败时抛出原始异常
     */
    <T> T executeWithRetry(Supplier<T> supplier, int maxAttempts, long retryIntervalMs);
}
