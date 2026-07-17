# Task 1: M1 - 基础设施搭建

## 模块概述
搭建Spring Boot + MyBatis-Plus项目骨架，配置MySQL、Redis、RocketMQ连接，建立统一日志和异常处理框架。

---

## 子步骤

### 1. 工程准备
- [ ] 初始化Spring Boot项目（Java 17+，Spring Boot 3.x）
- [ ] 添加Maven依赖：MyBatis-Plus、MySQL驱动、Redis、RocketMQ、Lombok等
- [ ] 配置项目目录结构（controller、service、mapper、entity、config等）
- [ ] 配置application.yml基础参数

### 2. 测试先行
- [ ] 编写Spring Boot启动测试类
- [ ] 编写MySQL连接测试（测试数据源配置）
- [ ] 编写Redis连接测试（测试RedisTemplate注入）
- [ ] 编写RocketMQ连接测试（测试Producer/Consumer注入）

### 3. 硬编码跑通
- [ ] 创建MySQL数据库smartfleet
- [ ] 执行建表SQL（tb_vehicle、tb_order、tb_grid_dispatch、tb_user）
- [ ] 配置数据源连接参数（硬编码到application.yml）
- [ ] 验证Spring Boot启动成功并能连接MySQL
- [ ] 验证Redis连接成功（RedisTemplate可正常操作）
- [ ] 验证RocketMQ连接成功（Producer可发送消息）

### 4. 骨架
- [ ] 创建实体类：Vehicle、Order、GridDispatch、User
- [ ] 创建Mapper接口：VehicleMapper、OrderMapper、GridDispatchMapper、UserMapper
- [ ] 创建Service接口和实现类骨架
- [ ] 创建Controller骨架（预留接口）
- [ ] 配置MyBatis-Plus分页插件

### 5. 数据加载
- [ ] 编写数据初始化脚本（插入测试数据）
- [ ] 创建Redis工具类封装（String/Hash操作、Lua脚本执行）
- [ ] 创建RocketMQ工具类封装（Producer/Consumer）
- [ ] 创建统一响应类Result<T>
- [ ] 创建业务异常枚举和异常类

### 6. 检索实现
- [ ] 实现车辆查询接口（按ID、按状态查询）
- [ ] 实现订单查询接口（按ID、按用户ID查询）
- [ ] 实现网格运力查询接口
- [ ] 实现用户查询接口

### 7. 集成具体实现
- [ ] 配置统一日志格式（logback-spring.xml）
- [ ] 实现traceId链路追踪（MDC过滤器）
- [ ] 实现全局异常处理器（@RestControllerAdvice）
- [ ] 集成SpringDoc/Swagger（API文档自动生成）
- [ ] 编写集成测试验证整体功能

---

## 测试用例记录 (test-record)

### 单元测试用例

#### 1. 正常测试用例
```java
@Test
void testSpringBootContextLoads() {
    // 验证Spring容器正常启动
    assertNotNull(applicationContext);
}

@Test
void testMySQLConnection() {
    // 验证MySQL连接正常
    assertNotNull(dataSource.getConnection());
}

@Test
void testRedisConnection() {
    // 验证Redis连接正常
    stringRedisTemplate.opsForValue().set("test", "value");
    assertEquals("value", stringRedisTemplate.opsForValue().get("test"));
}

@Test
void testRocketMQProducer() {
    // 验证Producer可发送消息
    assertNotNull(rocketMQTemplate);
}
```

#### 2. 边界测试用例
```java
@Test
void testMySQLConnectionTimeout() {
    // 测试数据库连接超时处理
    // 模拟网络延迟或数据库不可用
}

@Test
void testRedisConnectionFailure() {
    // 测试Redis连接失败时的降级处理
    // 验证系统仍能正常运行
}

@Test
void testRocketMQBrokerUnreachable() {
    // 测试RocketMQ Broker不可达时的处理
    // 验证消息发送失败的重试机制
}
```

#### 3. 失败测试用例
```java
@Test
void testInvalidDatabaseConfig() {
    // 测试错误的数据库配置
    // 预期：启动失败，抛出异常
    assertThrows(DataSourceProperties.DataSourceBeanCreationException.class, () -> {
        // 模拟错误配置
    });
}

@Test
void testInvalidRedisConfig() {
    // 测试错误的Redis配置
    // 预期：Redis操作失败，但系统不崩溃
}
```

### 集成测试用例

#### 1. 正常流程测试
```java
@Test
void testFullInfrastructureSetup() {
    // 测试完整基础设施搭建
    // 1. 验证Spring Boot启动
    // 2. 验证MySQL表创建
    // 3. 验证Redis基本操作
    // 4. 验证RocketMQ基本功能
    // 5. 验证统一日志输出
    // 6. 验证异常处理框架
}
```

#### 2. 异常场景测试
```java
@Test
void testDatabaseTableCreationFailure() {
    // 测试表创建失败时的处理
    // 验证错误日志记录
    // 验证系统优雅降级
}

@Test
void testRedisLuaScriptExecution() {
    // 测试Redis Lua脚本执行
    // 验证原子性操作
}
```

#### 3. 性能边界测试
```java
@Test
void testConcurrentDatabaseConnections() {
    // 测试并发数据库连接
    // 验证连接池配置有效性
}

@Test
void testRedisHighConcurrency() {
    // 测试Redis高并发操作
    // 验证连接池和超时配置
}
```

---

## 验收标准
- [ ] Spring Boot项目启动成功，无异常
- [ ] MySQL数据库连接正常，表结构创建成功
- [ ] Redis连接正常，基础操作可用
- [ ] RocketMQ连接正常，Producer/Consumer可用
- [ ] 统一日志格式生效，traceId可追踪
- [ ] 全局异常处理框架生效，统一异常响应格式
- [ ] 单元测试覆盖率 ≥ 80%
- [ ] 集成测试通过

## 依赖项
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- RocketMQ 5.0+

## 风险与注意事项
1. 确保所有中间件版本兼容
2. 注意数据库连接池配置（HikariCP）
3. Redis连接池配置（Lettuce/Jedis）
4. RocketMQ NameServer地址配置
5. 日志级别配置（生产环境与开发环境区分）