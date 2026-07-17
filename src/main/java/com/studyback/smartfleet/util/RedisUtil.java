package com.studyback.smartfleet.util;

import com.studyback.smartfleet.exception.BusinessException;
import com.studyback.smartfleet.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 * <p>封装常用的 Redis 操作，所有 key 必须有前缀（如 sf:vehicle:{id}）</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置缓存（带过期时间）
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败, key={}", key, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 连接失败, key=" + key);
        } catch (Exception e) {
            log.error("Redis set 操作异常, key={}", key, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 操作异常, key=" + key);
        }
    }

    /**
     * 设置缓存（默认秒为单位）
     */
    public void set(String key, Object value, long timeoutSeconds) {
        set(key, value, timeoutSeconds, TimeUnit.SECONDS);
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败, key={}", key, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 连接失败, key=" + key);
        } catch (Exception e) {
            log.error("Redis get 操作异常, key={}", key, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 操作异常, key=" + key);
        }
    }

    /**
     * 获取缓存并转换类型
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            return clazz.cast(value);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败, key={}", key, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 连接失败, key=" + key);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Redis get 操作异常, key={}", key, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 操作异常, key=" + key);
        }
    }

    /**
     * 删除缓存
     */
    public Boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败, key={}", key, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 连接失败, key=" + key);
        } catch (Exception e) {
            log.error("Redis delete 操作异常, key={}", key, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 操作异常, key=" + key);
        }
    }

    /**
     * 判断 key 是否存在
     */
    public Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败, key={}", key, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 连接失败, key=" + key);
        } catch (Exception e) {
            log.error("Redis hasKey 操作异常, key={}", key, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 操作异常, key=" + key);
        }
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return redisTemplate.expire(key, timeout, unit);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败, key={}", key, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 连接失败, key=" + key);
        } catch (Exception e) {
            log.error("Redis expire 操作异常, key={}", key, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 操作异常, key=" + key);
        }
    }

    /**
     * Hash 设置字段值
     */
    public void hSet(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败, key={}, field={}", key, field, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 连接失败, key=" + key + ", field=" + field);
        } catch (Exception e) {
            log.error("Redis hSet 操作异常, key={}, field={}", key, field, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 操作异常, key=" + key + ", field=" + field);
        }
    }

    /**
     * Hash 获取字段值
     */
    public Object hGet(String key, String field) {
        try {
            return redisTemplate.opsForHash().get(key, field);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败, key={}, field={}", key, field, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 连接失败, key=" + key + ", field=" + field);
        } catch (Exception e) {
            log.error("Redis hGet 操作异常, key={}, field={}", key, field, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 操作异常, key=" + key + ", field=" + field);
        }
    }

    /**
     * Hash 删除字段
     */
    public Long hDelete(String key, Object... fields) {
        try {
            return redisTemplate.opsForHash().delete(key, fields);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败, key={}", key, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 连接失败, key=" + key);
        } catch (Exception e) {
            log.error("Redis hDelete 操作异常, key={}", key, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 操作异常, key=" + key);
        }
    }

    /**
     * Hash 判断字段是否存在
     */
    public Boolean hHasKey(String key, String field) {
        try {
            return redisTemplate.opsForHash().hasKey(key, field);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败, key={}, field={}", key, field, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 连接失败, key=" + key + ", field=" + field);
        } catch (Exception e) {
            log.error("Redis hHasKey 操作异常, key={}, field={}", key, field, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 操作异常, key=" + key + ", field=" + field);
        }
    }

    /**
     * 执行 Lua 脚本
     *
     * @param script Lua 脚本
     * @param keys   key 列表
     * @param args   参数列表
     * @return 脚本执行结果
     */
    public Object executeLuaScript(String script, String key, Object... args) {
        try {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
            return redisTemplate.execute(redisScript, Collections.singletonList(key), args);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败, key={}", key, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 连接失败, key=" + key);
        } catch (Exception e) {
            log.error("Redis Lua 脚本执行异常, key={}", key, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis Lua 脚本执行异常, key=" + key);
        }
    }

    /**
     * 自增操作
     */
    public Long increment(String key) {
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败, key={}", key, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 连接失败, key=" + key);
        } catch (Exception e) {
            log.error("Redis increment 操作异常, key={}", key, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 操作异常, key=" + key);
        }
    }

    /**
     * 自增操作（指定步长）
     */
    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败, key={}", key, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 连接失败, key=" + key);
        } catch (Exception e) {
            log.error("Redis increment 操作异常, key={}", key, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Redis 操作异常, key=" + key);
        }
    }
}
