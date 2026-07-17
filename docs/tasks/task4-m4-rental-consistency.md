# Task 4: M4 - 高并发租赁一致性控制

## 模块概述
实现Redis Lua预占锁脚本、MySQL乐观锁二次校验、自旋重试机制，实现租赁订单创建、确认、完成、取消全流程接口，实现一键租赁接口。

---

## 子步骤

### 1. 工程准备
- [ ] 设计预占锁架构（Redis Lua脚本）
- [ ] 设计乐观锁机制（version字段）
- [ ] 创建订单实体类Order（包含version字段）
- [ ] 创建订单状态枚举OrderStatus
- [ ] 创建订单Mapper接口

### 2. 测试先行
- [ ] 编写Redis Lua预占锁脚本测试
- [ ] 编写MySQL乐观锁测试
- [ ] 编写自旋重试机制测试
- [ ] 编写订单创建接口测试
- [ ] 编写订单确认接口测试
- [ ] 编写订单完成接口测试
- [ ] 编写订单取消接口测试
- [ ] 编写一键租赁接口测试

### 3. 硬编码跑通
- [ ] 实现Redis Lua预占锁脚本（硬编码锁逻辑）
- [ ] 实现MySQL乐观锁二次校验（硬编码version检查）
- [ ] 实现自旋重试机制（硬编码重试次数和间隔）
- [ ] 实现订单创建逻辑（硬编码状态流转）
- [ ] 实现订单确认逻辑（硬编码状态检查）
- [ ] 验证预占锁和乐观锁可正常工作

### 4. 骨架
- [ ] 创建OrderService接口
- [ ] 创建OrderServiceImpl实现类
- [ ] 创建RedisLockService（预占锁服务）
- [ ] 创建OptimisticLockService（乐观锁服务）
- [ ] 创建RetryService（重试服务）
- [ ] 创建OrderController（订单接口）

### 5. 数据加载
- [ ] 实现Redis Lua预占锁脚本加载和执行
- [ ] 实现订单数据持久化（MySQL）
- [ ] 实现车辆状态同步更新（Redis缓存）
- [ ] 实现订单号生成策略（雪花算法或UUID）
- [ ] 创建订单日志记录

### 6. 检索实现
- [ ] 实现订单查询接口（按ID、按用户ID、按状态查询）
- [ ] 实现订单列表查询接口（分页、排序）
- [ ] 实现订单统计接口（按状态统计数量）
- [ ] 实现订单详情查询接口（包含车辆信息）

### 7. 集成具体实现
- [ ] 集成Redis预占锁与订单创建流程
- [ ] 集成MySQL乐观锁与订单更新流程
- [ ] 实现一键租赁接口（自动推荐 → 预占 → 创建订单）
- [ ] 实现订单超时自动取消（定时任务或Redis过期监听）
- [ ] 实现订单状态变更通知（WebSocket或消息队列）
- [ ] 编写集成测试验证完整租赁流程

---

## 测试用例记录 (test-record)

### 单元测试用例

#### 1. 正常测试用例
```java
@Test
void testRedisPreemptiveLock() {
    // 测试Redis预占锁
    Long vehicleId = 1L;
    Long userId = 100L;
    int lockSeconds = 300; // 5分钟
    
    boolean locked = redisLockService.tryLock(vehicleId, userId, lockSeconds);
    assertTrue(locked);
    
    // 验证锁存在
    assertTrue(redisLockService.isLocked(vehicleId));
    
    // 验证锁的持有者
    assertEquals(userId, redisLockService.getLockOwner(vehicleId));
}

@Test
void testRedisLockRelease() {
    // 测试Redis锁释放
    Long vehicleId = 1L;
    Long userId = 100L;
    
    redisLockService.tryLock(vehicleId, userId, 300);
    boolean released = redisLockService.releaseLock(vehicleId, userId);
    assertTrue(released);
    
    // 验证锁已释放
    assertFalse(redisLockService.isLocked(vehicleId));
}

@Test
void testMySQL乐观锁() {
    // 测试MySQL乐观锁
    Order order = new Order();
    order.setId(1L);
    order.setStatus(OrderStatus.CREATED);
    order.setVersion(1);
    
    // 第一次更新
    order.setStatus(OrderStatus.CONFIRMED);
    boolean updated = orderService.updateOrderWithOptimisticLock(order);
    assertTrue(updated);
    
    // 验证version增加
    Order updatedOrder = orderService.getOrderById(1L);
    assertEquals(2, updatedOrder.getVersion());
}

@Test
void testOrderCreation() {
    // 测试订单创建
    Long userId = 100L;
    Long vehicleId = 1L;
    double startLat = 30.5;
    double startLng = 114.3;
    
    Order order = orderService.createOrder(userId, vehicleId, startLat, startLng);
    
    assertNotNull(order);
    assertNotNull(order.getId());
    assertEquals(OrderStatus.CREATED, order.getStatus());
    assertEquals(userId, order.getUserId());
    assertEquals(vehicleId, order.getVehicleId());
}

@Test
void testOrderConfirmation() {
    // 测试订单确认
    Order order = createTestOrder();
    Order confirmedOrder = orderService.confirmOrder(order.getId(), order.getUserId());
    
    assertNotNull(confirmedOrder);
    assertEquals(OrderStatus.RUNNING, confirmedOrder.getStatus());
}

@Test
void testOrderCompletion() {
    // 测试订单完成
    Order order = createTestOrder();
    orderService.confirmOrder(order.getId(), order.getUserId());
    
    Order completedOrder = orderService.completeOrder(order.getId(), order.getUserId());
    
    assertNotNull(completedOrder);
    assertEquals(OrderStatus.COMPLETED, completedOrder.getStatus());
}

@Test
void testOrderCancellation() {
    // 测试订单取消
    Order order = createTestOrder();
    Order cancelledOrder = orderService.cancelOrder(order.getId(), order.getUserId());
    
    assertNotNull(cancelledOrder);
    assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
}

@Test
void testSpinRetry() {
    // 测试自旋重试机制
    AtomicInteger attempts = new AtomicInteger(0);
    
    retryService.executeWithRetry(() -> {
        attempts.incrementAndGet();
        if (attempts.get() < 3) {
            throw new ConcurrentModificationException("模拟并发冲突");
        }
        return "success";
    }, 5, 100);
    
    assertEquals(3, attempts.get());
}

@Test
void testQuickRent() {
    // 测试一键租赁
    Long userId = 100L;
    double lat = 30.5;
    double lng = 114.3;
    
    Order order = orderService.quickRent(userId, lat, lng);
    
    assertNotNull(order);
    assertNotNull(order.getId());
    assertEquals(OrderStatus.CREATED, order.getStatus());
}
```

#### 2. 边界测试用例
```java
@Test
void testRedisLockWithExpiredLock() {
    // 测试过期锁
    Long vehicleId = 1L;
    Long userId = 100L;
    
    // 创建锁，1秒过期
    redisLockService.tryLock(vehicleId, userId, 1);
    
    // 等待锁过期
    Thread.sleep(1100);
    
    // 验证锁已过期
    assertFalse(redisLockService.isLocked(vehicleId));
    
    // 其他用户可以获取锁
    Long otherUserId = 200L;
    boolean locked = redisLockService.tryLock(vehicleId, otherUserId, 300);
    assertTrue(locked);
}

@Test
void testRedisLockWithWrongOwner() {
    // 测试错误的锁持有者释放锁
    Long vehicleId = 1L;
    Long userId = 100L;
    Long wrongUserId = 200L;
    
    redisLockService.tryLock(vehicleId, userId, 300);
    
    // 错误的持有者尝试释放锁
    boolean released = redisLockService.releaseLock(vehicleId, wrongUserId);
    assertFalse(released);
    
    // 验证锁仍然存在
    assertTrue(redisLockService.isLocked(vehicleId));
}

@Test
void testMySQL乐观锁WithStaleVersion() {
    // 测试过时的version
    Order order = new Order();
    order.setId(1L);
    order.setStatus(OrderStatus.CREATED);
    order.setVersion(1);
    
    // 模拟并发更新：另一个线程先更新了
    Order concurrentOrder = orderService.getOrderById(1L);
    concurrentOrder.setStatus(OrderStatus.CONFIRMED);
    orderService.updateOrderWithOptimisticLock(concurrentOrder);
    
    // 当前线程使用旧version更新
    order.setStatus(OrderStatus.COMPLETED);
    boolean updated = orderService.updateOrderWithOptimisticLock(order);
    
    // 应该失败，因为version不匹配
    assertFalse(updated);
}

@Test
void testOrderCreationWithInvalidVehicle() {
    // 测试无效车辆创建订单
    Long userId = 100L;
    Long vehicleId = 999L; // 不存在的车辆
    
    assertThrows(BusinessException.class, () -> {
        orderService.createOrder(userId, vehicleId, 30.5, 114.3);
    });
}

@Test
void testOrderCreationWithUnavailableVehicle() {
    // 测试不可用车辆创建订单
    Long userId = 100L;
    Long vehicleId = 1L;
    
    // 设置车辆为不可用
    Vehicle vehicle = vehicleService.getVehicleById(vehicleId);
    vehicle.setStatus(VehicleStatus.RENTING);
    vehicleService.updateVehicle(vehicle);
    
    assertThrows(BusinessException.class, () -> {
        orderService.createOrder(userId, vehicleId, 30.5, 114.3);
    });
}

@Test
void testOrderConfirmationWithExpiredLock() {
    // 测试预占锁过期后确认订单
    Order order = createTestOrder();
    
    // 模拟预占锁过期
    redisLockService.releaseLock(order.getVehicleId(), order.getUserId());
    
    assertThrows(BusinessException.class, () -> {
        orderService.confirmOrder(order.getId(), order.getUserId());
    });
}

@Test
void testSpinRetryWithMaxAttempts() {
    // 测试达到最大重试次数
    AtomicInteger attempts = new AtomicInteger(0);
    
    assertThrows(ConcurrentModificationException.class, () -> {
        retryService.executeWithRetry(() -> {
            attempts.incrementAndGet();
            throw new ConcurrentModificationException("模拟并发冲突");
        }, 3, 100);
    });
    
    assertEquals(3, attempts.get());
}
```

#### 3. 失败测试用例
```java
@Test
void testRedisLockWithInvalidParams() {
    // 测试无效参数
    assertThrows(IllegalArgumentException.class, () -> {
        redisLockService.tryLock(null, 100L, 300);
    });
    
    assertThrows(IllegalArgumentException.class, () -> {
        redisLockService.tryLock(1L, null, 300);
    });
    
    assertThrows(IllegalArgumentException.class, () -> {
        redisLockService.tryLock(1L, 100L, -1);
    });
}

@Test
void testOrderCreationWithNullParams() {
    // 测试空参数创建订单
    assertThrows(IllegalArgumentException.class, () -> {
        orderService.createOrder(null, 1L, 30.5, 114.3);
    });
    
    assertThrows(IllegalArgumentException.class, () -> {
        orderService.createOrder(100L, null, 30.5, 114.3);
    });
}

@Test
void testOrderConfirmationWithWrongUser() {
    // 测试错误用户确认订单
    Order order = createTestOrder();
    Long wrongUserId = 200L;
    
    assertThrows(BusinessException.class, () -> {
        orderService.confirmOrder(order.getId(), wrongUserId);
    });
}

@Test
void testOrderCancellationWithWrongStatus() {
    // 测试错误状态取消订单
    Order order = createTestOrder();
    orderService.confirmOrder(order.getId(), order.getUserId());
    
    // 已确认的订单不能直接取消
    assertThrows(BusinessException.class, () -> {
        orderService.cancelOrder(order.getId(), order.getUserId());
    });
}
```

### 集成测试用例

#### 1. 正常流程测试
```java
@Test
void testFullRentalFlow() {
    // 测试完整租赁流程
    Long userId = 100L;
    Long vehicleId = 1L;
    double startLat = 30.5;
    double startLng = 114.3;
    
    // 1. 创建订单
    Order order = orderService.createOrder(userId, vehicleId, startLat, startLng);
    assertNotNull(order);
    assertEquals(OrderStatus.CREATED, order.getStatus());
    
    // 验证车辆状态已更新为RESERVED
    Vehicle vehicle = vehicleService.getVehicleById(vehicleId);
    assertEquals(VehicleStatus.RESERVED, vehicle.getStatus());
    
    // 2. 确认订单
    Order confirmedOrder = orderService.confirmOrder(order.getId(), userId);
    assertEquals(OrderStatus.RUNNING, confirmedOrder.getStatus());
    
    // 验证车辆状态已更新为RENTING
    vehicle = vehicleService.getVehicleById(vehicleId);
    assertEquals(VehicleStatus.RENTING, vehicle.getStatus());
    
    // 3. 完成订单
    Order completedOrder = orderService.completeOrder(order.getId(), userId);
    assertEquals(OrderStatus.COMPLETED, completedOrder.getStatus());
    
    // 验证车辆状态已更新为AVAILABLE
    vehicle = vehicleService.getVehicleById(vehicleId);
    assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
}

@Test
void testQuickRentFlow() {
    // 测试一键租赁流程
    Long userId = 100L;
    double lat = 30.5;
    double lng = 114.3;
    
    // 1. 一键租赁
    Order order = orderService.quickRent(userId, lat, lng);
    assertNotNull(order);
    assertEquals(OrderStatus.CREATED, order.getStatus());
    
    // 2. 确认订单
    Order confirmedOrder = orderService.confirmOrder(order.getId(), userId);
    assertEquals(OrderStatus.RUNNING, confirmedOrder.getStatus());
    
    // 3. 完成订单
    Order completedOrder = orderService.completeOrder(order.getId(), userId);
    assertEquals(OrderStatus.COMPLETED, completedOrder.getStatus());
}

@Test
void testConcurrentOrderCreation() {
    // 测试并发订单创建
    Long vehicleId = 1L;
    int threadCount = 10;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    List<Future<Order>> futures = new ArrayList<>();
    
    for (int i = 0; i < threadCount; i++) {
        final Long userId = 100L + i;
        futures.add(executor.submit(() -> {
            try {
                return orderService.createOrder(userId, vehicleId, 30.5, 114.3);
            } finally {
                latch.countDown();
            }
        }));
    }
    
    assertDoesNotThrow(() -> {
        latch.await(10, TimeUnit.SECONDS);
    });
    
    // 只有一个订单应该成功
    long successCount = futures.stream()
        .filter(f -> {
            try {
                return f.get() != null;
            } catch (Exception e) {
                return false;
            }
        })
        .count();
    
    assertEquals(1, successCount);
    
    executor.shutdown();
}

@Test
void testConcurrent同一Vehicle() {
    // 测试并发抢同一辆车
    Long vehicleId = 1L;
    Long userId1 = 100L;
    Long userId2 = 200L;
    
    // 用户1预占
    boolean locked1 = redisLockService.tryLock(vehicleId, userId1, 300);
    assertTrue(locked1);
    
    // 用户2尝试预占（应该失败）
    boolean locked2 = redisLockService.tryLock(vehicleId, userId2, 300);
    assertFalse(locked2);
    
    // 用户1释放锁
    redisLockService.releaseLock(vehicleId, userId1);
    
    // 用户2再次尝试预占（应该成功）
    locked2 = redisLockService.tryLock(vehicleId, userId2, 300);
    assertTrue(locked2);
}
```

#### 2. 异常场景测试
```java
@Test
void testOrderCreationWithRedisFailure() {
    // 测试Redis故障时的降级处理
    // 模拟Redis不可用
    when(redisLockService.tryLock(any(), any(), anyInt())).thenThrow(new RedisConnectionFailureException("Redis不可用"));
    
    Long userId = 100L;
    Long vehicleId = 1L;
    
    // 应该降级为仅MySQL乐观锁
    Order order = orderService.createOrder(userId, vehicleId, 30.5, 114.3);
    assertNotNull(order);
}

@Test
void testOrderConfirmationWithVehicleStatusChange() {
    // 测试预占期间车辆状态变更
    Order order = createTestOrder();
    
    // 模拟管理员将车辆标记为维修
    Vehicle vehicle = vehicleService.getVehicleById(order.getVehicleId());
    vehicle.setStatus(VehicleStatus.REPAIRING);
    vehicleService.updateVehicle(vehicle);
    
    // 尝试确认订单
    assertThrows(BusinessException.class, () -> {
        orderService.confirmOrder(order.getId(), order.getUserId());
    });
    
    // 验证订单已自动取消
    Order cancelledOrder = orderService.getOrderById(order.getId());
    assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
}

@Test
void testOrderTimeoutCancellation() {
    // 测试订单超时自动取消
    Order order = createTestOrder();
    
    // 模拟超时（设置创建时间为5分钟前）
    order.setCreateTime(LocalDateTime.now().minusMinutes(5));
    orderService.updateOrder(order);
    
    // 触发超时检查
    orderService.checkAndCancelExpiredOrders();
    
    // 验证订单已取消
    Order cancelledOrder = orderService.getOrderById(order.getId());
    assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
    
    // 验证车辆已释放
    Vehicle vehicle = vehicleService.getVehicleById(order.getVehicleId());
    assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
}

@Test
void testQuickRentWithNoAvailableVehicles() {
    // 测试无车辆可租
    Long userId = 100L;
    double lat = 90.0; // 极地，应该没有车辆
    double lng = 0.0;
    
    assertThrows(BusinessException.class, () -> {
        orderService.quickRent(userId, lat, lng);
    });
}

@Test
void testQuickRentWithConcurrentCompetition() {
    // 测试一键租赁并发竞争
    Long userId = 100L;
    double lat = 30.5;
    double lng = 114.3;
    
    // 模拟第一辆车被抢走
    doReturn(false).when(redisLockService).tryLock(eq(1L), eq(userId), anyInt());
    doReturn(true).when(redisLockService).tryLock(eq(2L), eq(userId), anyInt());
    
    Order order = orderService.quickRent(userId, lat, lng);
    assertNotNull(order);
    assertEquals(2L, order.getVehicleId());
}
```

#### 3. 性能边界测试
```java
@Test
void testHighConcurrencyOrderCreation() {
    // 测试高并发订单创建
    int threadCount = 100;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failCount = new AtomicInteger(0);
    
    for (int i = 0; i < threadCount; i++) {
        final Long userId = 100L + i;
        final Long vehicleId = 1L + (i % 10); // 10辆车
        
        executor.submit(() -> {
            try {
                Order order = orderService.createOrder(userId, vehicleId, 30.5, 114.3);
                if (order != null) {
                    successCount.incrementAndGet();
                }
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });
    }
    
    assertDoesNotThrow(() -> {
        latch.await(30, TimeUnit.SECONDS);
    });
    
    // 验证没有超卖：每辆车最多一个订单
    assertTrue(successCount.get() <= 10);
    
    executor.shutdown();
}

@Test
void testLockPerformance() {
    // 测试锁性能
    int iterations = 1000;
    long start = System.currentTimeMillis();
    
    for (int i = 0; i < iterations; i++) {
        Long vehicleId = 1L + (i % 100);
        Long userId = 100L + i;
        
        redisLockService.tryLock(vehicleId, userId, 300);
        redisLockService.releaseLock(vehicleId, userId);
    }
    
    long time = System.currentTimeMillis() - start;
    
    // 验证性能在可接受范围内（如1000次操作在1秒内）
    assertTrue(time < 1000, "锁操作性能过低: " + time + "ms for " + iterations + " operations");
}
```

---

## 验收标准
- [ ] Redis Lua预占锁脚本正常工作（原子性校验 + 状态修改 + 过期设置）
- [ ] MySQL乐观锁二次校验正常工作（version字段）
- [ ] 自旋重试机制正常工作（抢占失败时自动重评分与二次分配）
- [ ] 订单创建接口正常工作（校验车辆状态、预占车辆、创建订单）
- [ ] 订单确认接口正常工作（校验订单状态、更新车辆状态）
- [ ] 订单完成接口正常工作（更新订单状态、释放车辆）
- [ ] 订单取消接口正常工作（更新订单状态、释放车辆）
- [ ] 一键租赁接口正常工作（自动推荐 → 预占 → 创建订单）
- [ ] 订单超时自动取消功能正常工作
- [ ] 高并发场景无超卖
- [ ] 单元测试覆盖率 ≥ 80%
- [ ] 集成测试通过

## 依赖项
- Redis（用于预占锁）
- MySQL（用于订单存储和乐观锁）
- 车辆调度引擎（用于推荐车辆）
- 车辆状态机（用于状态流转）

## 风险与注意事项
1. Redis预占锁需要考虑Redis故障时的降级策略
2. 乐观锁需要考虑高并发时的重试机制
3. 订单超时需要考虑定时任务的精度和性能
4. 一键租赁需要考虑推荐车辆的并发竞争
5. 需要记录详细的操作日志，便于问题排查