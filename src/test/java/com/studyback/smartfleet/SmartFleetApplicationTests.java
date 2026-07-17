package com.studyback.smartfleet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Spring Boot 启动测试
 * <p>验证 Spring 容器正常启动</p>
 */
@SpringBootTest
class SmartFleetApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 验证 Spring 容器正常启动，核心 Bean 已注入
     */
    @Test
    void contextLoads() {
        assertNotNull(applicationContext, "Spring 容器不应为 null");
        // 验证核心 Bean 已注册
        assertNotNull(applicationContext.getBean(SmartFleetApplication.class), "主启动类 Bean 应存在");
    }
}
