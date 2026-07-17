package com.studyback.smartfleet;

import com.studyback.smartfleet.entity.Vehicle;
import com.studyback.smartfleet.mapper.VehicleMapper;
import com.studyback.smartfleet.response.ApiResponse;
import com.studyback.smartfleet.response.ResultCode;
import com.studyback.smartfleet.util.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 基础设施集成测试
 * <p>验证完整基础设施搭建：MySQL、Redis、统一响应、异常处理</p>
 */
@SpringBootTest
class InfrastructureIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private VehicleMapper vehicleMapper;

    /**
     * 验证 MySQL 查询正常（通过 MyBatis-Plus 查询车辆表）
     */
    @Test
    void testMybatisPlusQuery() {
        var vehicles = vehicleMapper.selectList(null);
        assertNotNull(vehicles, "查询结果不应为 null");
        // 验证测试数据已加载
        assertFalse(vehicles.isEmpty(), "应有测试车辆数据");
    }

    /**
     * 验证 Redis 工具类操作正常
     */
    @Test
    void testRedisUtilOperations() {
        String key = "sf:test:integration";
        String value = "integration-test-value";
        try {
            // 设置缓存
            redisUtil.set(key, value, 60);
            // 获取缓存
            Object result = redisUtil.get(key);
            assertNotNull(result, "Redis 缓存值不应为 null");
            assertEquals(value, result.toString());
            // 判断 key 存在
            assertTrue(redisUtil.hasKey(key));
            // 删除缓存
            redisUtil.delete(key);
            assertFalse(redisUtil.hasKey(key));
        } finally {
            redisUtil.delete(key);
        }
    }

    /**
     * 验证统一响应类正常工作
     */
    @Test
    void testApiResponse() {
        // 成功响应
        ApiResponse<String> successResponse = ApiResponse.success("test-data");
        assertEquals(ResultCode.SUCCESS.getCode(), successResponse.getCode());
        assertEquals("test-data", successResponse.getData());

        // 失败响应
        ApiResponse<Void> failResponse = ApiResponse.fail(ResultCode.NOT_FOUND);
        assertEquals(ResultCode.NOT_FOUND.getCode(), failResponse.getCode());
    }
}
