# Task 5: M5 - 车辆状态机管理

## 模块概述
实现车辆状态机（AVAILABLE / RESERVED / RENTING / CHARGING / REPAIRING），实现状态转移矩阵校验（拦截非法状态跃迁），状态变更同步更新Redis缓存。

---

## 子步骤

### 1. 工程准备
- [ ] 设计车辆状态机架构
- [ ] 创建车辆状态枚举VehicleStatus
- [ ] 创建状态转移矩阵（合法状态转移映射）
- [ ] 创建状态转移事件枚举StateEvent
- [ ] 创建状态机上下文类StateMachineContext

### 2. 测试先行
- [ ] 编写车辆状态机初始化测试
- [ ] 编写合法状态转移测试（所有合法路径）
- [ ] 编写非法状态转移测试（所有非法路径）
- [ ] 编写状态转移事件触发测试
- [ ] 编写状态变更Redis缓存同步测试

### 3. 硬编码跑通
- [ ] 实现车辆状态机（硬编码状态转移逻辑）
- [ ] 实现状态转移矩阵校验（硬编码合法/非法转移）
- [ ] 实现状态变更事件发布（硬编码事件处理）
- [ ] 实现Redis缓存同步（硬编码缓存更新逻辑）
- [ ] 验证状态机可正常工作

### 4. 骨架
- [ ] 创建VehicleStateMachine接口
- [ ] 创建VehicleStateMachineImpl实现类
- [ ] 创建StateTransitionValidator（状态转移校验器）
- [ ] 创建StateChangeEvent（状态变更事件）
- [ ] 创建StateChangeListener（状态变更监听器）
- [ ] 创建VehicleStateController（状态查询接口）

### 5. 数据加载
- [ ] 实现状态转移矩阵配置加载（从数据库或配置文件）
- [ ] 实现车辆状态数据加载（从MySQL）
- [ ] 实现车辆状态缓存加载（从Redis）
- [ ] 实现状态变更日志记录
- [ ] 创建状态转移配置表和实体类

### 6. 检索实现
- [ ] 实现车辆状态查询接口（根据车辆ID查询状态）
- [ ] 实现车辆状态历史查询接口（查询状态变更历史）
- [ ] 实现状态统计接口（按状态统计车辆数量）
- [ ] 实现状态转移规则查询接口

### 7. 集成具体实现
- [ ] 集成状态机与车辆服务（状态变更时触发状态机）
- [ ] 集成状态机与订单服务（订单状态变更触发车辆状态变更）
- [ ] 集成状态机与Redis缓存（状态变更同步更新缓存）
- [ ] 实现状态转移事件监听器（记录日志、发送通知）
- [ ] 编写集成测试验证完整状态流转

---

## 测试用例记录 (test-record)

### 单元测试用例

#### 1. 正常测试用例
```java
@Test
void testStateMachineInitialization() {
    // 测试状态机初始化
    VehicleStateMachine stateMachine = new VehicleStateMachine();
    assertNotNull(stateMachine);
    
    // 验证初始状态
    assertEquals(VehicleStatus.AVAILABLE, stateMachine.getCurrentState());
}

@Test
void testAvailableToReserved() {
    // 测试AVAILABLE -> RESERVED转移
    VehicleStateMachine stateMachine = new VehicleStateMachine(VehicleStatus.AVAILABLE);
    
    boolean transitioned = stateMachine.sendEvent(StateEvent.RESERVE);
    assertTrue(transitioned);
    assertEquals(VehicleStatus.RESERVED, stateMachine.getCurrentState());
}

@Test
void testReservedToRenting() {
    // 测试RESERVED -> RENTING转移
    VehicleStateMachine stateMachine = new VehicleStateMachine(VehicleStatus.RESERVED);
    
    boolean transitioned = stateMachine.sendEvent(StateEvent.START_RENT);
    assertTrue(transitioned);
    assertEquals(VehicleStatus.RENTING, stateMachine.getCurrentState());
}

@Test
void testRentingToAvailable() {
    // 测试RENTING -> AVAILABLE转移
    VehicleStateMachine stateMachine = new VehicleStateMachine(VehicleStatus.RENTING);
    
    boolean transitioned = stateMachine.sendEvent(StateEvent.END_RENT);
    assertTrue(transitioned);
    assertEquals(VehicleStatus.AVAILABLE, stateMachine.getCurrentState());
}

@Test
void testAvailableToCharging() {
    // 测试AVAILABLE -> CHARGING转移
    VehicleStateMachine stateMachine = new VehicleStateMachine(VehicleStatus.AVAILABLE);
    
    boolean transitioned = stateMachine.sendEvent(StateEvent.START_CHARGE);
    assertTrue(transitioned);
    assertEquals(VehicleStatus.CHARGING, stateMachine.getCurrentState());
}

@Test
void testChargingToAvailable() {
    // 测试CHARGING -> AVAILABLE转移
    VehicleStateMachine stateMachine = new VehicleStateMachine(VehicleStatus.CHARGING);
    
    boolean transitioned = stateMachine.sendEvent(StateEvent.END_CHARGE);
    assertTrue(transitioned);
    assertEquals(VehicleStatus.AVAILABLE, stateMachine.getCurrentState());
}

@Test
void testAvailableToRepairing() {
    // 测试AVAILABLE -> REPAIRING转移
    VehicleStateMachine stateMachine = new VehicleStateMachine(VehicleStatus.AVAILABLE);
    
    boolean transitioned = stateMachine.sendEvent(StateEvent.START_REPAIR);
    assertTrue(transitioned);
    assertEquals(VehicleStatus.REPAIRING, stateMachine.getCurrentState());
}

@Test
void testRepairingToAvailable() {
    // 测试REPAIRING -> AVAILABLE转移
    VehicleStateMachine stateMachine = new VehicleStateMachine(VehicleStatus.REPAIRING);
    
    boolean transitioned = stateMachine.sendEvent(StateEvent.END_REPAIR);
    assertTrue(transitioned);
    assertEquals(VehicleStatus.AVAILABLE, stateMachine.getCurrentState());
}

@Test
void testReservedToAvailable() {
    // 测试RESERVED -> AVAILABLE转移（取消预占）
    VehicleStateMachine stateMachine = new VehicleStateMachine(VehicleStatus.RESERVED);
    
    boolean transitioned = stateMachine.sendEvent(StateEvent.CANCEL_RESERVE);
    assertTrue(transitioned);
    assertEquals(VehicleStatus.AVAILABLE, stateMachine.getCurrentState());
}

@Test
void testStateChangeCacheSync() {
    // 测试状态变更Redis缓存同步
    Long vehicleId = 1L;
    VehicleStateMachine stateMachine = new VehicleStateMachine(VehicleStatus.AVAILABLE);
    
    // 模拟状态变更
    stateMachine.sendEvent(StateEvent.RESERVE);
    
    // 验证Redis缓存已更新
    String cachedStatus = redisTemplate.opsForValue().get("vehicle:status:" + vehicleId);
    assertEquals(VehicleStatus.RESERVED.name(), cachedStatus);
}

@Test
void testStateTransitionValidation() {
    // 测试状态转移校验
    StateTransitionValidator validator = new StateTransitionValidator();
    
    // 验证合法转移
    assertTrue(validator.isValidTransition(VehicleStatus.AVAILABLE, VehicleStatus.RESERVED));
    assertTrue(validator.isValidTransition(VehicleStatus.RESERVED, VehicleStatus.RENTING));
    assertTrue(validator.isValidTransition(VehicleStatus.RENTING, VehicleStatus.AVAILABLE));
    
    // 验证非法转移
    assertFalse(validator.isValidTransition(VehicleStatus.AVAILABLE, VehicleStatus.RENTING));
    assertFalse(validator.isValidTransition(VehicleStatus.RENTING, VehicleStatus.RESERVED));
}
```

#### 2. 边界测试用例
```java
@Test
void testInvalidStateTransition() {
    // 测试非法状态转移
    VehicleStateMachine stateMachine = new VehicleStateMachine(VehicleStatus.AVAILABLE);
    
    // AVAILABLE -> RENTING 是非法的（必须先 RESERVED）
    boolean transitioned = stateMachine.sendEvent(StateEvent.START_RENT);
    assertFalse(transitioned);
    assertEquals(VehicleStatus.AVAILABLE, stateMachine.getCurrentState());
}

@Test
void testRentingToReserved() {
    // 测试RENTING -> RESERVED非法转移
    VehicleStateMachine stateMachine = new VehicleStateMachine(VehicleStatus.RENTING);
    
    boolean transitioned = stateMachine.sendEvent(StateEvent.RESERVE);
    assertFalse(transitioned);
    assertEquals(VehicleStatus.RENTING, stateMachine.getCurrentState());
}

@Test
void testChargingToRenting() {
    // 测试CHARGING -> RENTING非法转移
    VehicleStateMachine stateMachine = new VehicleStateMachine(VehicleStatus.CHARGING);
    
    boolean transitioned = stateMachine.sendEvent(StateEvent.START_RENT);
    assertFalse(transitioned);
    assertEquals(VehicleStatus.CHARGING, stateMachine.getCurrentState());
}

@Test
void testRepairingToRenting() {
    // 测试REPAIRING -> RENTING非法转移
    VehicleStateMachine stateMachine = new VehicleStateMachine(VehicleStatus.REPAIRING);
    
    boolean transitioned = stateMachine.sendEvent(StateEvent.START_RENT);
    assertFalse(transitioned);
    assertEquals(VehicleStatus.REPAIRING, stateMachine.getCurrentState());
}

@Test
void testNullEvent() {
    // 测试空事件
    VehicleStateMachine stateMachine = new VehicleStateMachine(VehicleStatus.AVAILABLE);
    
    assertThrows(IllegalArgumentException.class, () -> {
        stateMachine.sendEvent(null);
    });
}

@Test
void testInvalidCurrentState() {
    // 测试无效的当前状态
    assertThrows(IllegalArgumentException.class, () -> {
        new VehicleStateMachine(null);
    });
}

@Test
void testStateTransitionWithSameState() {
    // 测试相同状态转移
    VehicleStateMachine stateMachine = new VehicleStateMachine(VehicleStatus.AVAILABLE);
    
    // AVAILABLE -> AVAILABLE 应该是非法的
    boolean transitioned = stateMachine.sendEvent(StateEvent.RESERVE);
    assertTrue(transitioned);
    
    // 再次尝试AVAILABLE -> AVAILABLE
    transitioned = stateMachine.sendEvent(StateEvent.CANCEL_RESERVE);
    assertTrue(transitioned);
    assertEquals(VehicleStatus.AVAILABLE, stateMachine.getCurrentState());
}
```

#### 3. 失败测试用例
```java
@Test
void testStateMachineWithCorruptedState() {
    // 测试状态机损坏状态
    VehicleStateMachine stateMachine = new VehicleStateMachine(VehicleStatus.AVAILABLE);
    
    // 模拟状态损坏
    stateMachine.setCurrentState(null);
    
    assertThrows(IllegalStateException.class, () -> {
        stateMachine.sendEvent(StateEvent.RESERVE);
    });
}

@Test
void testStateTransitionWithDatabaseFailure() {
    // 测试数据库故障时的状态转移
    VehicleStateMachine stateMachine = new VehicleStateMachine(VehicleStatus.AVAILABLE);
    
    // 模拟数据库故障
    when(vehicleRepository.save(any())).thenThrow(new DataAccessException("数据库故障") {});
    
    assertThrows(DataAccessException.class, () -> {
        stateMachine.sendEvent(StateEvent.RESERVE);
    });
}

@Test
void testStateTransitionWithRedisFailure() {
    // 测试Redis故障时的状态转移
    VehicleStateMachine stateMachine = new VehicleStateMachine(VehicleStatus.AVAILABLE);
    
    // 模拟Redis故障
    when(redisTemplate.opsForValue()).thenThrow(new RedisConnectionFailureException("Redis故障") {});
    
    // 状态转移应该仍然成功（降级处理）
    boolean transitioned = stateMachine.sendEvent(StateEvent.RESERVE);
    assertTrue(transitioned);
    assertEquals(VehicleStatus.RESERVED, stateMachine.getCurrentState());
}

@Test
void testStateChangeListenerException() {
    // 测试状态变更监听器异常
    VehicleStateMachine stateMachine = new VehicleStateMachine(VehicleStatus.AVAILABLE);
    
    // 添加一个抛出异常的监听器
    stateMachine.addStateChangeListener(event -> {
        throw new RuntimeException("监听器异常");
    });
    
    // 状态转移应该仍然成功
    boolean transitioned = stateMachine.sendEvent(StateEvent.RESERVE);
    assertTrue(transitioned);
    assertEquals(VehicleStatus.RESERVED, stateMachine.getCurrentState());
}
```

### 集成测试用例

#### 1. 正常流程测试
```java
@Test
void testFullVehicleLifecycle() {
    // 测试完整车辆生命周期
    Long vehicleId = 1L;
    VehicleStateMachine stateMachine = new VehicleStateMachine(VehicleStatus.AVAILABLE);
    
    // 1. AVAILABLE -> RESERVED（用户预占）
    stateMachine.sendEvent(StateEvent.RESERVE);
    assertEquals(VehicleStatus.RESERVED, stateMachine.getCurrentState());
    
    // 2. RESERVED -> RENTING（用户确认租赁）
    stateMachine.sendEvent(StateEvent.START_RENT);
    assertEquals(VehicleStatus.RENTING, stateMachine.getCurrentState());
    
    // 3. RENTING -> AVAILABLE（用户还车）
    stateMachine.sendEvent(StateEvent.END_RENT);
    assertEquals(VehicleStatus.AVAILABLE, stateMachine.getCurrentState());
    
    // 4. AVAILABLE -> CHARGING（开始充电）
    stateMachine.sendEvent(StateEvent.START_CHARGE);
    assertEquals(VehicleStatus.CHARGING, stateMachine.getCurrentState());
    
    // 5. CHARGING -> AVAILABLE（充电完成）
    stateMachine.sendEvent(StateEvent.END_CHARGE);
    assertEquals(VehicleStatus.AVAILABLE, stateMachine.getCurrentState());
    
    // 6. AVAILABLE -> REPAIRING（开始维修）
    stateMachine.sendEvent(StateEvent.START_REPAIR);
    assertEquals(VehicleStatus.REPAIRING, stateMachine.getCurrentState());
    
    // 7. REPAIRING -> AVAILABLE（维修完成）
    stateMachine.sendEvent(StateEvent.END_REPAIR);
    assertEquals(VehicleStatus.AVAILABLE, stateMachine.getCurrentState());
}

@Test
void testVehicleStatusQuery() {
    // 测试车辆状态查询
    Long vehicleId = 1L;
    
    // 设置车辆状态
    Vehicle vehicle = vehicleService.getVehicleById(vehicleId);
    vehicle.setStatus(VehicleStatus.AVAILABLE);
    vehicleService.updateVehicle(vehicle);
    
    // 查询状态
    VehicleStatus status = vehicleStateService.getVehicleStatus(vehicleId);
    assertEquals(VehicleStatus.AVAILABLE, status);
    
    // 查询缓存状态
    VehicleStatus cachedStatus = vehicleStateService.getVehicleStatusFromCache(vehicleId);
    assertEquals(VehicleStatus.AVAILABLE, cachedStatus);
}

@Test
void testStateChangeHistory() {
    // 测试状态变更历史
    Long vehicleId = 1L;
    
    // 执行多次状态变更
    vehicleStateService.changeVehicleStatus(vehicleId, StateEvent.RESERVE);
    vehicleStateService.changeVehicleStatus(vehicleId, StateEvent.START_RENT);
    vehicleStateService.changeVehicleStatus(vehicleId, StateEvent.END_RENT);
    
    // 查询历史
    List<StateChangeRecord> history = vehicleStateService.getStateChangeHistory(vehicleId);
    
    assertNotNull(history);
    assertEquals(3, history.size());
    
    // 验证历史记录顺序
    assertEquals(VehicleStatus.AVAILABLE, history.get(0).getFromStatus());
    assertEquals(VehicleStatus.RESERVED, history.get(0).getToStatus());
    
    assertEquals(VehicleStatus.RESERVED, history.get(1).getFromStatus());
    assertEquals(VehicleStatus.RENTING, history.get(1).getToStatus());
    
    assertEquals(VehicleStatus.RENTING, history.get(2).getFromStatus());
    assertEquals(VehicleStatus.AVAILABLE, history.get(2).getToStatus());
}

@Test
void testStateChangeEventListener() {
    // 测试状态变更事件监听
    Long vehicleId = 1L;
    AtomicInteger eventCount = new AtomicInteger(0);
    
    // 添加监听器
    vehicleStateService.addStateChangeListener(event -> {
        eventCount.incrementAndGet();
        assertEquals(vehicleId, event.getVehicleId());
    });
    
    // 执行状态变更
    vehicleStateService.changeVehicleStatus(vehicleId, StateEvent.RESERVE);
    vehicleStateService.changeVehicleStatus(vehicleId, StateEvent.START_RENT);
    
    // 验证监听器被调用
    assertEquals(2, eventCount.get());
}
```

#### 2. 异常场景测试
```java
@Test
void testIllegalStateTransitionInService() {
    // 测试服务层非法状态转移
    Long vehicleId = 1L;
    
    // 设置车辆为RENTING状态
    Vehicle vehicle = vehicleService.getVehicleById(vehicleId);
    vehicle.setStatus(VehicleStatus.RENTING);
    vehicleService.updateVehicle(vehicle);
    
    // 尝试非法转移：RENTING -> RESERVED
    assertThrows(BusinessException.class, () -> {
        vehicleStateService.changeVehicleStatus(vehicleId, StateEvent.RESERVE);
    });
    
    // 验证状态未改变
    VehicleStatus status = vehicleStateService.getVehicleStatus(vehicleId);
    assertEquals(VehicleStatus.RENTING, status);
}

@Test
void testConcurrentStateChange() {
    // 测试并发状态变更
    Long vehicleId = 1L;
    int threadCount = 10;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failCount = new AtomicInteger(0);
    
    for (int i = 0; i < threadCount; i++) {
        executor.submit(() -> {
            try {
                vehicleStateService.changeVehicleStatus(vehicleId, StateEvent.RESERVE);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });
    }
    
    assertDoesNotThrow(() -> {
        latch.await(10, TimeUnit.SECONDS);
    });
    
    // 只有一个应该成功
    assertEquals(1, successCount.get());
    assertEquals(threadCount - 1, failCount.get());
    
    executor.shutdown();
}

@Test
void testStateChangeWithVehicleNotFound() {
    // 测试车辆不存在时的状态变更
    Long nonExistentVehicleId = 999L;
    
    assertThrows(BusinessException.class, () -> {
        vehicleStateService.changeVehicleStatus(nonExistentVehicleId, StateEvent.RESERVE);
    });
}

@Test
void testStateChangeWithInvalidEvent() {
    // 测试无效事件
    Long vehicleId = 1L;
    
    assertThrows(IllegalArgumentException.class, () -> {
        vehicleStateService.changeVehicleStatus(vehicleId, null);
    });
}
```

#### 3. 性能边界测试
```java
@Test
void testStateChangePerformance() {
    // 测试状态变更性能
    Long vehicleId = 1L;
    int iterations = 1000;
    
    long start = System.currentTimeMillis();
    
    for (int i = 0; i < iterations; i++) {
        // 循环执行状态变更
        vehicleStateService.changeVehicleStatus(vehicleId, StateEvent.RESERVE);
        vehicleStateService.changeVehicleStatus(vehicleId, StateEvent.START_RENT);
        vehicleStateService.changeVehicleStatus(vehicleId, StateEvent.END_RENT);
    }
    
    long time = System.currentTimeMillis() - start;
    
    // 验证性能在可接受范围内（如1000次操作在2秒内）
    assertTrue(time < 2000, "状态变更性能过低: " + time + "ms for " + iterations + " operations");
}

@Test
void testConcurrentStateQuery() {
    // 测试并发状态查询
    Long vehicleId = 1L;
    int threadCount = 100;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    
    for (int i = 0; i < threadCount; i++) {
        executor.submit(() -> {
            try {
                VehicleStatus status = vehicleStateService.getVehicleStatus(vehicleId);
                assertNotNull(status);
            } finally {
                latch.countDown();
            }
        });
    }
    
    assertDoesNotThrow(() -> {
        latch.await(10, TimeUnit.SECONDS);
    });
    
    executor.shutdown();
}
```

---

## 验收标准
- [ ] 车辆状态机正常工作（支持所有状态）
- [ ] 状态转移矩阵校验正常工作（拦截非法状态跃迁）
- [ ] 状态变更同步更新Redis缓存
- [ ] 状态变更事件发布和监听正常工作
- [ ] 状态查询接口正常工作
- [ ] 状态历史查询接口正常工作
- [ ] 单元测试覆盖率 ≥ 80%
- [ ] 集成测试通过
- [ ] 所有合法/非法状态转移路径均有测试用例

## 依赖项
- 车辆实体和Mapper
- Redis（用于状态缓存）
- 状态转移配置

## 风险与注意事项
1. 状态机需要考虑并发场景（乐观锁或分布式锁）
2. Redis缓存需要考虑数据一致性（状态变更时同步更新）
3. 状态转移需要记录详细日志，便于问题排查
4. 状态机需要支持扩展（新增状态或转移规则）
5. 需要考虑状态变更的通知机制（WebSocket或消息队列）