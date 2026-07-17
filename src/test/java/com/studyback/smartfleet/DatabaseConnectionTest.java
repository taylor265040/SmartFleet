package com.studyback.smartfleet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MySQL 数据库连接测试
 * <p>验证数据源配置正确，能够正常连接 MySQL</p>
 */
@SpringBootTest
class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    /**
     * 验证数据源注入成功
     */
    @Test
    void testDataSourceInjected() {
        assertNotNull(dataSource, "数据源不应为 null");
    }

    /**
     * 验证 MySQL 连接正常，能获取数据库元数据
     */
    @Test
    void testMySQLConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "数据库连接不应为 null");
            DatabaseMetaData metaData = connection.getMetaData();
            assertNotNull(metaData, "数据库元数据不应为 null");
            assertTrue(metaData.getDatabaseProductName().contains("MySQL"),
                    "数据库应为 MySQL");
        }
    }
}
