package com.studyback.smartfleet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Redis 连接测试
 * <p>验证 Redis 连接正常，基础操作可用</p>
 */
@SpringBootTest
class RedisConnectionTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 验证 RedisTemplate 注入成功
     */
    @Test
    void testRedisTemplateInjected() {
        assertNotNull(stringRedisTemplate, "StringRedisTemplate 不应为 null");
    }

    /**
     * 验证 Redis 基础读写操作
     */
    @Test
    void testRedisConnection() {
        String key = "sf:test:connection";
        String value = "smartfleet-test";
        try {
            stringRedisTemplate.opsForValue().set(key, value);
            String result = stringRedisTemplate.opsForValue().get(key);
            assertEquals(value, result, "Redis 读写操作应一致");
        } finally {
            stringRedisTemplate.delete(key);
        }
    }
}
