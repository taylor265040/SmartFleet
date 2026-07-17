# Task 7: M7 - 实时数据处理与监控

## 模块概述
实现Cache Aside读写模式（车辆状态优先走Redis，失效回源MySQL），实现RocketMQ异步削峰（车辆位置更新、里程上报、订单落库异步投递），实现WebSocket推送（区域车辆分布、核心业务指标实时推送），提供API接口供前端获取监控数据。

---

## 子步骤

### 1. 工程准备
- [ ] 设计Cache Aside架构
- [ ] 设计RocketMQ消息架构
- [ ] 设计WebSocket推送架构
- [ ] 创建缓存工具类CacheAsideTemplate
- [ ] 创建消息Producer/Consumer模板

### 2. 测试先行
- [ ] 编写Cache Aside读写测试
- [ ] 编写缓存失效测试
- [ ] 编写RocketMQ消息发送测试
- [ ] 编写RocketMQ消息消费测试
- [ ] 编写WebSocket连接测试
- [ ] 编写WebSocket消息推送测试
- [ ] 编写监控数据API测试

### 3. 硬编码跑通
- [ ] 实现Cache Aside读写模式（硬编码缓存逻辑）
- [ ] 实现缓存失效策略（硬编码TTL和失效逻辑）
- [ ] 实现RocketMQ消息发送（硬编码Producer逻辑）
- [ ] 实现RocketMQ消息消费（硬编码Consumer逻辑）
- [ ] 实现WebSocket连接管理（硬编码连接处理）
- [ ] 验证Cache Aside和RocketMQ可正常工作

### 4. 骨架
- [ ] 创建CacheAsideService接口和实现类
- [ ] 创建RocketMQProducerService接口和实现类
- [ ] 创建RocketMQConsumerService接口和实现类
- [ ] 创建WebSocketService接口和实现类
- [ ] 创建MonitoringController（监控数据接口）
- [ ] 创建WebSocketHandler（WebSocket处理器）

### 5. 数据加载
- [ ] 实现缓存配置加载（TTL、最大大小等）
- [ ] 实现RocketMQ Topic配置加载
- [ ] 实现WebSocket订阅配置加载
- [ ] 实现监控指标数据加载
- [ ] 创建缓存配置表和实体类

### 6. 检索实现
- [ ] 实现车辆状态缓存查询接口
- [ ] 实现监控数据查询接口（车辆分布、订单统计等）
- [ ] 实现实时指标查询接口（在线用户、活跃订单等）
- [ ] 实现历史数据查询接口（按时间范围查询）
- [ ] 实现告警规则查询接口

### 7. 集成具体实现
- [ ] 集成Cache Aside与车辆服务（车辆状态缓存）
- [ ] 集成Cache Aside与订单服务（订单数据缓存）
- [ ] 集成RocketMQ与车辆位置更新（异步处理）
- [ ] 集成RocketMQ与里程上报（异步处理）
- [ ] 集成RocketMQ与订单落库（异步处理）
- [ ] 集成WebSocket与区域车辆分布推送
- [ ] 集成WebSocket与核心业务指标推送
- [ ] 编写集成测试验证完整实时处理流程

---

## 测试用例记录 (test-record)

### 单元测试用例

#### 1. 正常测试用例
```java
@Test
void testCacheAsideRead() {
    // 测试Cache Aside读模式
    Long vehicleId = 1L;
    
    // 模拟Redis缓存为空
    when(redisTemplate.opsForValue().get("vehicle:" + vehicleId)).thenReturn(null);
    
    // 模拟数据库查询
    Vehicle vehicle = createVehicle(vehicleId, VehicleStatus.AVAILABLE);
    when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
    
    // 执行Cache Aside读
    Vehicle result = cacheAsideService.getVehicle(vehicleId);
    
    assertNotNull(result);
    assertEquals(vehicleId, result.getId());
    
    // 验证数据库查询被调用
    verify(vehicleRepository).findById(vehicleId);
    
    // 验证结果被缓存
    verify(redisTemplate.opsForValue()).set("vehicle:" + vehicleId, vehicle, 30, TimeUnit.MINUTES);
}

@Test
void testCacheAsideReadFromCache() {
    // 测试从缓存读取
    Long vehicleId = 1L;
    
    // 模拟Redis缓存有数据
    Vehicle cachedVehicle = createVehicle(vehicleId, VehicleStatus.AVAILABLE);
    when(redisTemplate.opsForValue().get("vehicle:" + vehicleId)).thenReturn(cachedVehicle);
    
    // 执行Cache Aside读
    Vehicle result = cacheAsideService.getVehicle(vehicleId);
    
    assertNotNull(result);
    assertEquals(vehicleId, result.getId());
    
    // 验证数据库查询未被调用
    verify(vehicleRepository, never()).findById(any());
}

@Test
void testCacheAsideWrite() {
    // 测试Cache Aside写模式
    Long vehicleId = 1L;
    Vehicle vehicle = createVehicle(vehicleId, VehicleStatus.AVAILABLE);
    
    // 执行Cache Aside写
    cacheAsideService.updateVehicle(vehicle);
    
    // 验证数据库更新被调用
    verify(vehicleRepository).save(vehicle);
    
    // 验证缓存被更新
    verify(redisTemplate.opsForValue()).set("vehicle:" + vehicleId, vehicle, 30, TimeUnit.MINUTES);
}

@Test
void testCacheAsideDelete() {
    // 测试Cache Aside删除
    Long vehicleId = 1L;
    
    // 执行Cache Aside删除
    cacheAsideService.deleteVehicle(vehicleId);
    
    // 验证数据库删除被调用
    verify(vehicleRepository).deleteById(vehicleId);
    
    // 验证缓存被删除
    verify(redisTemplate).delete("vehicle:" + vehicleId);
}

@Test
void testRocketMQMessageSending() {
    // 测试RocketMQ消息发送
    String topic = "vehicle-location-update";
    String message = "{\"vehicleId\":1,\"lat\":30.5,\"lng\":114.3}";
    
    rocketMQProducerService.send(topic, message);
    
    // 验证消息发送
    verify(rocketMQTemplate).send(eq(topic), any(Message.class));
}

@Test
void testRocketMQMessageSendingWithDelay() {
    // 测试RocketMQ延迟消息发送
    String topic = "order-timeout-check";
    String message = "{\"orderId\":1,\"timeout\":300}";
    int delayLevel = 3; // 延迟级别
    
    rocketMQProducerService.sendWithDelay(topic, message, delayLevel);
    
    // 验证延迟消息发送
    verify(rocketMQTemplate).send(eq(topic), any(Message.class), eq(delayLevel));
}

@Test
void testRocketMQMessageConsumption() {
    // 测试RocketMQ消息消费
    String topic = "vehicle-location-update";
    String message = "{\"vehicleId\":1,\"lat\":30.5,\"lng\":114.3}";
    
    // 模拟消息接收
    MessageExt messageExt = new MessageExt();
    messageExt.setTopic(topic);
    messageExt.setBody(message.getBytes());
    
    // 执行消费
    rocketMQConsumerService.handleMessage(messageExt);
    
    // 验证业务逻辑被调用
    verify(vehicleLocationService).updateLocation(eq(1L), eq(30.5), eq(114.3));
}

@Test
void testWebSocketConnection() {
    // 测试WebSocket连接
    WebSocketSession session = mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(true);
    
    webSocketService.handleConnection(session);
    
    // 验证连接被管理
    assertTrue(webSocketService.isSessionActive(session.getId()));
}

@Test
void testWebSocketMessageSending() {
    // 测试WebSocket消息发送
    WebSocketSession session = mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(true);
    
    String message = "{\"type\":\"vehicle-update\",\"data\":{}}";
    
    webSocketService.sendMessage(session, message);
    
    // 验证消息发送
    verify(session).sendMessage(any(TextMessage.class));
}

@Test
void testWebSocketBroadcast() {
    // 测试WebSocket广播
    WebSocketSession session1 = mock(WebSocketSession.class);
    WebSocketSession session2 = mock(WebSocketSession.class);
    when(session1.isOpen()).thenReturn(true);
    when(session2.isOpen()).thenReturn(true);
    
    webSocketService.handleConnection(session1);
    webSocketService.handleConnection(session2);
    
    String message = "{\"type\":\"broadcast\",\"data\":{}}";
    
    webSocketService.broadcast(message);
    
    // 验证广播发送
    verify(session1).sendMessage(any(TextMessage.class));
    verify(session2).sendMessage(any(TextMessage.class));
}

@Test
void testMonitoringDataQuery() {
    // 测试监控数据查询
    // 模拟车辆分布数据
    when(vehicleRepository.countByStatus(VehicleStatus.AVAILABLE)).thenReturn(100L);
    when(vehicleRepository.countByStatus(VehicleStatus.RENTING)).thenReturn(50L);
    when(vehicleRepository.countByStatus(VehicleStatus.RESERVED)).thenReturn(20L);
    
    MonitoringData data = monitoringService.getMonitoringData();
    
    assertNotNull(data);
    assertEquals(100L, data.getAvailableVehicles());
    assertEquals(50L, data.getRentingVehicles());
    assertEquals(20L, data.getReservedVehicles());
}
```

#### 2. 边界测试用例
```java
@Test
void testCacheAsideWithRedisFailure() {
    // 测试Redis故障时的Cache Aside
    Long vehicleId = 1L;
    
    // 模拟Redis故障
    when(redisTemplate.opsForValue()).thenThrow(new RedisConnectionFailureException("Redis故障") {});
    
    // 模拟数据库查询
    Vehicle vehicle = createVehicle(vehicleId, VehicleStatus.AVAILABLE);
    when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
    
    // 执行Cache Aside读（应该降级到数据库）
    Vehicle result = cacheAsideService.getVehicle(vehicleId);
    
    assertNotNull(result);
    assertEquals(vehicleId, result.getId());
    
    // 验证数据库查询被调用
    verify(vehicleRepository).findById(vehicleId);
}

@Test
void testCacheAsideWithDatabaseFailure() {
    // 测试数据库故障时的Cache Aside
    Long vehicleId = 1L;
    
    // 模拟Redis缓存为空
    when(redisTemplate.opsForValue().get("vehicle:" + vehicleId)).thenReturn(null);
    
    // 模拟数据库故障
    when(vehicleRepository.findById(vehicleId)).thenThrow(new DataAccessException("数据库故障") {});
    
    // 执行Cache Aside读（应该抛出异常）
    assertThrows(DataAccessException.class, () -> {
        cacheAsideService.getVehicle(vehicleId);
    });
}

@Test
void testCacheAsideWithNullValue() {
    // 测试缓存空值
    Long vehicleId = 999L;
    
    // 模拟Redis缓存为空
    when(redisTemplate.opsForValue().get("vehicle:" + vehicleId)).thenReturn(null);
    
    // 模拟数据库查询返回空
    when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());
    
    // 执行Cache Aside读
    Vehicle result = cacheAsideService.getVehicle(vehicleId);
    
    assertNull(result);
    
    // 验证空值被缓存（防止缓存穿透）
    verify(redisTemplate.opsForValue()).set("vehicle:" + vehicleId, null, 5, TimeUnit.MINUTES);
}

@Test
void testRocketMQWithBrokerFailure() {
    // 测试RocketMQ Broker故障
    String topic = "test-topic";
    String message = "test-message";
    
    // 模拟Broker故障
    doThrow(new MQClientException("Broker故障", null)).when(rocketMQTemplate).send(any(), any());
    
    // 执行消息发送（应该重试）
    assertThrows(MQClientException.class, () -> {
        rocketMQProducerService.send(topic, message);
    });
}

@Test
void testRocketMQWithInvalidMessage() {
    // 测试无效消息
    String topic = "test-topic";
    String invalidMessage = null;
    
    assertThrows(IllegalArgumentException.class, () -> {
        rocketMQProducerService.send(topic, invalidMessage);
    });
}

@Test
void testWebSocketWithClosedSession() {
    // 测试已关闭的WebSocket会话
    WebSocketSession session = mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(false);
    
    String message = "{\"type\":\"test\",\"data\":{}}";
    
    // 执行消息发送（应该跳过）
    webSocketService.sendMessage(session, message);
    
    // 验证消息未发送
    verify(session, never()).sendMessage(any());
}

@Test
void testWebSocketWithLargeMessage() {
    // 测试大消息
    WebSocketSession session = mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(true);
    
    // 创建大消息（1MB）
    String largeMessage = "x".repeat(1024 * 1024);
    
    // 执行消息发送（应该分片或压缩）
    webSocketService.sendMessage(session, largeMessage);
    
    // 验证消息发送
    verify(session).sendMessage(any(TextMessage.class));
}

@Test
void testMonitoringDataWithNoData() {
    // 测试无数据时的监控查询
    when(vehicleRepository.countByStatus(any())).thenReturn(0L);
    when(orderRepository.countByStatus(any())).thenReturn(0L);
    
    MonitoringData data = monitoringService.getMonitoringData();
    
    assertNotNull(data);
    assertEquals(0L, data.getAvailableVehicles());
    assertEquals(0L, data.getRentingVehicles());
    assertEquals(0L, data.getReservedVehicles());
}
```

#### 3. 失败测试用例
```java
@Test
void testCacheAsideWithCorruptedData() {
    // 测试损坏的缓存数据
    Long vehicleId = 1L;
    
    // 模拟Redis返回损坏数据
    when(redisTemplate.opsForValue().get("vehicle:" + vehicleId)).thenReturn("corrupted-data");
    
    // 执行Cache Aside读（应该从数据库重新加载）
    Vehicle result = cacheAsideService.getVehicle(vehicleId);
    
    // 验证数据库查询被调用
    verify(vehicleRepository).findById(vehicleId);
}

@Test
void testRocketMQWithSerializationFailure() {
    // 测试序列化失败
    String topic = "test-topic";
    Object invalidObject = new Object(); // 无法序列化
    
    assertThrows(Exception.class, () -> {
        rocketMQProducerService.sendObject(topic, invalidObject);
    });
}

@Test
void testWebSocketWithNetworkFailure() {
    // 测试网络故障
    WebSocketSession session = mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(true);
    doThrow(new IOException("网络故障")).when(session).sendMessage(any());
    
    String message = "{\"type\":\"test\",\"data\":{}}";
    
    // 执行消息发送（应该处理异常）
    assertDoesNotThrow(() -> {
        webSocketService.sendMessage(session, message);
    });
    
    // 验证连接被标记为无效
    assertFalse(webSocketService.isSessionActive(session.getId()));
}

@Test
void testMonitoringDataWithDatabaseTimeout() {
    // 测试数据库超时
    when(vehicleRepository.countByStatus(any())).thenThrow(new QueryTimeoutException("查询超时"));
    
    // 执行监控数据查询（应该降级或返回默认值）
    MonitoringData data = monitoringService.getMonitoringData();
    
    // 验证返回默认值或降级结果
    assertNotNull(data);
}
```

### 集成测试用例

#### 1. 正常流程测试
```java
@Test
void testFullCacheAsideFlow() {
    // 测试完整Cache Aside流程
    Long vehicleId = 1L;
    
    // 1. 首次读取（从数据库加载）
    Vehicle vehicle = createVehicle(vehicleId, VehicleStatus.AVAILABLE);
    when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
    
    Vehicle result1 = cacheAsideService.getVehicle(vehicleId);
    assertNotNull(result1);
    verify(vehicleRepository).findById(vehicleId);
    
    // 2. 第二次读取（从缓存加载）
    when(redisTemplate.opsForValue().get("vehicle:" + vehicleId)).thenReturn(vehicle);
    
    Vehicle result2 = cacheAsideService.getVehicle(vehicleId);
    assertNotNull(result2);
    verify(vehicleRepository, times(1)).findById(vehicleId); // 只调用一次
    
    // 3. 更新车辆
    vehicle.setStatus(VehicleStatus.RENTING);
    cacheAsideService.updateVehicle(vehicle);
    
    // 4. 验证缓存已更新
    verify(redisTemplate.opsForValue()).set("vehicle:" + vehicleId, vehicle, 30, TimeUnit.MINUTES);
    
    // 5. 删除车辆
    cacheAsideService.deleteVehicle(vehicleId);
    
    // 6. 验证缓存已删除
    verify(redisTemplate).delete("vehicle:" + vehicleId);
}

@Test
void testFullRocketMQFlow() {
    // 测试完整RocketMQ流程
    String topic = "vehicle-location-update";
    
    // 1. 发送消息
    String message = "{\"vehicleId\":1,\"lat\":30.5,\"lng\":114.3}";
    rocketMQProducerService.send(topic, message);
    
    // 2. 验证消息发送
    verify(rocketMQTemplate).send(eq(topic), any(Message.class));
    
    // 3. 模拟消息消费
    MessageExt messageExt = new MessageExt();
    messageExt.setTopic(topic);
    messageExt.setBody(message.getBytes());
    
    rocketMQConsumerService.handleMessage(messageExt);
    
    // 4. 验证业务逻辑被调用
    verify(vehicleLocationService).updateLocation(eq(1L), eq(30.5), eq(114.3));
}

@Test
void testFullWebSocketFlow() {
    // 测试完整WebSocket流程
    WebSocketSession session = mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(true);
    
    // 1. 建立连接
    webSocketService.handleConnection(session);
    assertTrue(webSocketService.isSessionActive(session.getId()));
    
    // 2. 订阅主题
    webSocketService.subscribe(session.getId(), "vehicle-updates");
    
    // 3. 接收消息
    String message = "{\"type\":\"subscribe\",\"topic\":\"vehicle-updates\"}";
    webSocketService.handleMessage(session, new TextMessage(message));
    
    // 4. 发送更新
    String update = "{\"type\":\"vehicle-update\",\"vehicleId\":1,\"status\":\"AVAILABLE\"}";
    webSocketService.sendToSubscribers("vehicle-updates", update);
    
    // 5. 验证消息发送
    verify(session).sendMessage(any(TextMessage.class));
    
    // 6. 断开连接
    webSocketService.handleDisconnection(session);
    assertFalse(webSocketService.isSessionActive(session.getId()));
}

@Test
void testRealTimeMonitoringFlow() {
    // 测试实时监控流程
    // 1. 模拟车辆状态变更
    Long vehicleId = 1L;
    Vehicle vehicle = createVehicle(vehicleId, VehicleStatus.AVAILABLE);
    
    // 2. 更新车辆状态
    vehicle.setStatus(VehicleStatus.RENTING);
    cacheAsideService.updateVehicle(vehicle);
    
    // 3. 发送状态变更消息
    String message = "{\"vehicleId\":1,\"status\":\"RENTING\"}";
    rocketMQProducerService.send("vehicle-status-change", message);
    
    // 4. 消费消息并更新监控数据
    MessageExt messageExt = new MessageExt();
    messageExt.setBody(message.getBytes());
    rocketMQConsumerService.handleMessage(messageExt);
    
    // 5. 推送监控更新
    String update = "{\"type\":\"monitoring-update\",\"available\":99,\"renting\":51}";
    webSocketService.broadcast(update);
    
    // 6. 查询监控数据
    MonitoringData data = monitoringService.getMonitoringData();
    assertNotNull(data);
}
```

#### 2. 异常场景测试
```java
@Test
void testCacheAsideWithRedisAndDatabaseFailure() {
    // 测试Redis和数据库同时故障
    Long vehicleId = 1L;
    
    // 模拟Redis故障
    when(redisTemplate.opsForValue()).thenThrow(new RedisConnectionFailureException("Redis故障") {});
    
    // 模拟数据库故障
    when(vehicleRepository.findById(vehicleId)).thenThrow(new DataAccessException("数据库故障") {});
    
    // 执行Cache Aside读（应该抛出异常）
    assertThrows(DataAccessException.class, () -> {
        cacheAsideService.getVehicle(vehicleId);
    });
}

@Test
void testRocketMQWithConsumerFailure() {
    // 测试消费者处理失败
    String topic = "test-topic";
    String message = "{\"invalid\":\"data\"}";
    
    // 模拟消息接收
    MessageExt messageExt = new MessageExt();
    messageExt.setTopic(topic);
    messageExt.setBody(message.getBytes());
    
    // 模拟消费者处理失败
    doThrow(new RuntimeException("处理失败")).when(vehicleLocationService).updateLocation(any(), any(), any());
    
    // 执行消费（应该重试或进入死信队列）
    assertDoesNotThrow(() -> {
        rocketMQConsumerService.handleMessage(messageExt);
    });
}

@Test
void testWebSocketWithHighConcurrency() {
    // 测试WebSocket高并发
    int sessionCount = 1000;
    List<WebSocketSession> sessions = new ArrayList<>();
    
    for (int i = 0; i < sessionCount; i++) {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        sessions.add(session);
        webSocketService.handleConnection(session);
    }
    
    // 广播消息
    String message = "{\"type\":\"broadcast\",\"data\":{}}";
    
    long start = System.currentTimeMillis();
    webSocketService.broadcast(message);
    long time = System.currentTimeMillis() - start;
    
    // 验证性能在可接受范围内（如1000个会话广播在1秒内）
    assertTrue(time < 1000, "WebSocket广播性能过低: " + time + "ms for " + sessionCount + " sessions");
    
    // 验证所有会话都收到消息
    sessions.forEach(session -> {
        try {
            verify(session).sendMessage(any(TextMessage.class));
        } catch (IOException e) {
            fail("消息发送失败");
        }
    });
}

@Test
void testMonitoringDataWithPartialFailure() {
    // 测试部分数据源故障
    // 模拟车辆数据正常
    when(vehicleRepository.countByStatus(VehicleStatus.AVAILABLE)).thenReturn(100L);
    
    // 模拟订单数据故障
    when(orderRepository.countByStatus(OrderStatus.RUNNING)).thenThrow(new DataAccessException("数据库故障") {});
    
    // 执行监控数据查询（应该降级处理）
    MonitoringData data = monitoringService.getMonitoringData();
    
    // 验证返回部分数据
    assertNotNull(data);
    assertEquals(100L, data.getAvailableVehicles());
    assertEquals(0L, data.getRunningOrders()); // 降级为默认值
}
```

#### 3. 性能边界测试
```java
@Test
void testCacheAsidePerformance() {
    // 测试Cache Aside性能
    int iterations = 10000;
    Long vehicleId = 1L;
    
    // 模拟Redis缓存命中
    Vehicle vehicle = createVehicle(vehicleId, VehicleStatus.AVAILABLE);
    when(redisTemplate.opsForValue().get("vehicle:" + vehicleId)).thenReturn(vehicle);
    
    long start = System.currentTimeMillis();
    
    for (int i = 0; i < iterations; i++) {
        cacheAsideService.getVehicle(vehicleId);
    }
    
    long time = System.currentTimeMillis() - start;
    
    // 验证性能在可接受范围内（如10000次操作在1秒内）
    assertTrue(time < 1000, "Cache Aside性能过低: " + time + "ms for " + iterations + " operations");
}

@Test
void testRocketMQPerformance() {
    // 测试RocketMQ性能
    int iterations = 1000;
    String topic = "test-topic";
    String message = "{\"test\":\"data\"}";
    
    long start = System.currentTimeMillis();
    
    for (int i = 0; i < iterations; i++) {
        rocketMQProducerService.send(topic, message);
    }
    
    long time = System.currentTimeMillis() - start;
    
    // 验证性能在可接受范围内（如1000次操作在2秒内）
    assertTrue(time < 2000, "RocketMQ性能过低: " + time + "ms for " + iterations + " operations");
}

@Test
void testWebSocketPerformance() {
    // 测试WebSocket性能
    int sessionCount = 100;
    int messageCount = 100;
    
    List<WebSocketSession> sessions = new ArrayList<>();
    for (int i = 0; i < sessionCount; i++) {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        sessions.add(session);
        webSocketService.handleConnection(session);
    }
    
    long start = System.currentTimeMillis();
    
    for (int i = 0; i < messageCount; i++) {
        String message = "{\"type\":\"update\",\"data\":{\"index\":" + i + "}}";
        webSocketService.broadcast(message);
    }
    
    long time = System.currentTimeMillis() - start;
    
    // 验证性能在可接受范围内（如100个会话100条消息在5秒内）
    assertTrue(time < 5000, "WebSocket性能过低: " + time + "ms for " + sessionCount + " sessions and " + messageCount + " messages");
}
```

---

## 验收标准
- [ ] Cache Aside读写模式正常工作（优先走Redis，失效回源MySQL）
- [ ] RocketMQ异步削峰正常工作（车辆位置更新、里程上报、订单落库异步投递）
- [ ] WebSocket推送正常工作（区域车辆分布、核心业务指标实时推送）
- [ ] 监控数据API正常工作
- [ ] 缓存失效策略正常工作
- [ ] 消息重试机制正常工作
- [ ] 单元测试覆盖率 ≥ 80%
- [ ] 集成测试通过

## 依赖项
- Redis（用于缓存）
- RocketMQ（用于消息队列）
- WebSocket（用于实时推送）
- 车辆数据、订单数据

## 风险与注意事项
1. Cache Aside需要考虑缓存一致性（更新策略）
2. RocketMQ需要考虑消息丢失和重复消费
3. WebSocket需要考虑连接管理和心跳检测
4. 监控数据需要考虑实时性和准确性
5. 需要考虑系统资源限制（连接数、内存等）