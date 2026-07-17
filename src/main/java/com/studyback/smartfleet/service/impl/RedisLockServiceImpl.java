package com.studyback.smartfleet.service.impl;

import com.studyback.smartfleet.service.RedisLockService;
import com.studyback.smartfleet.util.RedisUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Redis 预占锁服务实现类
 * <p>基于 Redis Lua 脚本实现车辆预占锁，保证原子性操作</p>
 * <p>锁 key 格式：sf:lock:vehicle:{vehicleId}，value 为 userId</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLockServiceImpl implements RedisLockService {

    private final RedisUtil redisUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    /** 锁 key 前缀 */
    private static final String LOCK_KEY_PREFIX = "sf:lock:vehicle:";

    /** 默认锁过期时间（秒）：5 分钟 */
    private static final int DEFAULT_LOCK_SECONDS = 300;

    /** Lua 脚本：预占锁 */
    private DefaultRedisScript<Long> preemptiveLockScript;

    /** Lua 脚本：释放锁（原子性校验+删除） */
    private DefaultRedisScript<Long> releaseLockScript;

    /**
     * 初始化时加载 Lua 脚本
     */
    @PostConstruct
    public void init() {
        preemptiveLockScript = new DefaultRedisScript<>();
        preemptiveLockScript.setScriptSource(
                new ResourceScriptSource(new ClassPathResource("lua/preemptive_lock.lua")));
        preemptiveLockScript.setResultType(Long.class);

        releaseLockScript = new DefaultRedisScript<>();
        releaseLockScript.setScriptSource(
                new ResourceScriptSource(new ClassPathResource("lua/release_lock.lua")));
        releaseLockScript.setResultType(Long.class);
    }

    @Override
    public boolean tryLock(Long vehicleId, Long userId, int lockSeconds) {
        if (vehicleId == null || userId == null) {
            throw new IllegalArgumentException("车辆ID和用户ID不能为空");
        }
        if (lockSeconds <= 0) {
            throw new IllegalArgumentException("锁过期时间必须大于0");
        }

        String key = buildLockKey(vehicleId);
        log.info("尝试获取预占锁: vehicleId={}, userId={}, lockSeconds={}", vehicleId, userId, lockSeconds);

        // 执行 Lua 脚本，保证原子性
        Long result = redisTemplate.execute(
                preemptiveLockScript,
                Collections.singletonList(key),
                String.valueOf(userId),
                String.valueOf(lockSeconds));

        if (result == null) {
            log.error("Lua 脚本执行返回 null: vehicleId={}", vehicleId);
            return false;
        }

        switch (result.intValue()) {
            case 1:
                log.info("预占锁获取成功: vehicleId={}, userId={}", vehicleId, userId);
                return true;
            case -1:
                log.info("已是锁持有者，已续期: vehicleId={}, userId={}", vehicleId, userId);
                return true;
            case 0:
                log.info("预占锁获取失败，已被其他人锁定: vehicleId={}, userId={}", vehicleId, userId);
                return false;
            default:
                log.error("Lua 脚本返回未知结果: vehicleId={}, result={}", vehicleId, result);
                return false;
        }
    }

    @Override
    public boolean releaseLock(Long vehicleId, Long userId) {
        if (vehicleId == null || userId == null) {
            throw new IllegalArgumentException("车辆ID和用户ID不能为空");
        }

        String key = buildLockKey(vehicleId);
        log.info("尝试释放预占锁: vehicleId={}, userId={}", vehicleId, userId);

        // 执行 Lua 脚本，保证原子性（校验持有者 + 删除）
        Long result = redisTemplate.execute(
                releaseLockScript,
                Collections.singletonList(key),
                String.valueOf(userId));

        if (result == null) {
            log.error("Lua 脚本执行返回 null: vehicleId={}", vehicleId);
            return false;
        }

        if (result == 1) {
            log.info("预占锁释放成功: vehicleId={}, userId={}", vehicleId, userId);
            return true;
        } else {
            log.info("释放锁失败，不是持有者或锁不存在: vehicleId={}, userId={}", vehicleId, userId);
            return false;
        }
    }

    @Override
    public boolean isLocked(Long vehicleId) {
        if (vehicleId == null) {
            throw new IllegalArgumentException("车辆ID不能为空");
        }
        String key = buildLockKey(vehicleId);
        return Boolean.TRUE.equals(redisUtil.hasKey(key));
    }

    @Override
    public Long getLockOwner(Long vehicleId) {
        if (vehicleId == null) {
            throw new IllegalArgumentException("车辆ID不能为空");
        }
        String key = buildLockKey(vehicleId);
        Object owner = redisUtil.get(key);
        if (owner == null) {
            return null;
        }
        return Long.valueOf(String.valueOf(owner));
    }

    /**
     * 构建锁 key
     *
     * @param vehicleId 车辆ID
     * @return 完整的 Redis key
     */
    private String buildLockKey(Long vehicleId) {
        return LOCK_KEY_PREFIX + vehicleId;
    }
}
