# Task 3: M3 - 智能车辆调度引擎

## 模块概述
实现多维加权评分引擎，支持不同场景评分策略（普通时段、高峰时段、低电量召回），支持运行时动态切换评分权重，实现地图区域车辆推荐接口和车辆列表推荐接口。

---

## 子步骤

### 1. 工程准备
- [ ] 设计评分引擎架构（策略模式）
- [ ] 创建评分维度枚举（距离、电量、空闲时长、健康度）
- [ ] 创建评分策略接口ScoringStrategy
- [ ] 创建评分结果类ScoringResult
- [ ] 创建车辆推荐请求/响应DTO

### 2. 测试先行
- [ ] 编写距离评分计算测试
- [ ] 编写电量评分计算测试
- [ ] 编写空闲时长评分计算测试
- [ ] 编写健康度评分计算测试
- [ ] 编写多维度加权评分测试
- [ ] 编写策略模式切换测试

### 3. 硬编码跑通
- [ ] 实现距离评分算法（硬编码权重和计算公式）
- [ ] 实现电量评分算法（硬编码权重和计算公式）
- [ ] 实现空闲时长评分算法（硬编码权重和计算公式）
- [ ] 实现健康度评分算法（硬编码权重和计算公式）
- [ ] 实现多维度加权评分计算（硬编码权重组合）
- [ ] 验证评分计算结果正确性

### 4. 骨架
- [ ] 创建VehicleScoringService接口
- [ ] 创建VehicleScoringServiceImpl实现类
- [ ] 创建ScoringStrategyFactory策略工厂
- [ ] 创建不同场景策略实现：
  - NormalScoringStrategy（普通时段）
  - PeakHourScoringStrategy（高峰时段）
  - LowBatteryScoringStrategy（低电量召回）
- [ ] 创建VehicleRecommendController

### 5. 数据加载
- [ ] 实现评分权重配置加载（从数据库或配置文件）
- [ ] 实现车辆数据加载（从Redis缓存或MySQL）
- [ ] 实现车辆位置数据加载（经纬度坐标）
- [ ] 实现车辆状态数据加载（电量、空闲时长、健康度）
- [ ] 创建评分权重配置表和实体类

### 6. 检索实现
- [ ] 实现地图区域车辆推荐接口（限定数量，按评分排序返回带坐标的车辆列表）
- [ ] 实现车辆列表推荐接口（不含坐标，仅返回列表信息）
- [ ] 实现评分排序算法（支持多维度排序）
- [ ] 实现分页查询支持
- [ ] 实现结果缓存机制（Redis缓存推荐结果）

### 7. 集成具体实现
- [ ] 集成车辆调度引擎与车辆服务
- [ ] 集成评分引擎与订单服务（一键租赁使用）
- [ ] 实现运行时动态切换评分权重
- [ ] 实现评分策略热更新（不重启服务）
- [ ] 编写集成测试验证完整推荐流程
- [ ] 性能优化（批量计算、并行处理）

---

## 测试用例记录 (test-record)

### 单元测试用例

#### 1. 正常测试用例
```java
@Test
void testDistanceScoreCalculation() {
    // 测试距离评分计算
    double distance = 2.5; // 2.5公里
    double score = scoringService.calculateDistanceScore(distance);
    
    assertTrue(score >= 0 && score <= 100);
    // 距离越近，评分越高
    double closerScore = scoringService.calculateDistanceScore(1.0);
    assertTrue(closerScore > score);
}

@Test
void testBatteryScoreCalculation() {
    // 测试电量评分计算
    int batteryLevel = 80; // 80%
    double score = scoringService.calculateBatteryScore(batteryLevel);
    
    assertTrue(score >= 0 && score <= 100);
    // 电量越高，评分越高
    double higherBatteryScore = scoringService.calculateBatteryScore(95);
    assertTrue(higherBatteryScore > score);
}

@Test
void testIdleTimeScoreCalculation() {
    // 测试空闲时长评分计算
    long idleMinutes = 30; // 空闲30分钟
    double score = scoringService.calculateIdleTimeScore(idleMinutes);
    
    assertTrue(score >= 0 && score <= 100);
    // 空闲时间适中，评分最高（太短可能刚还车，太长可能有问题）
}

@Test
void testHealthScoreCalculation() {
    // 测试健康度评分计算
    int healthScore = 85; // 健康度85
    double score = scoringService.calculateHealthScore(healthScore);
    
    assertTrue(score >= 0 && score <= 100);
    // 健康度越高，评分越高
    double higherHealthScore = scoringService.calculateHealthScore(95);
    assertTrue(higherHealthScore > score);
}

@Test
void testWeightedScoreCalculation() {
    // 测试多维度加权评分计算
    Vehicle vehicle = new Vehicle();
    vehicle.setDistance(2.5);
    vehicle.setBatteryLevel(80);
    vehicle.setIdleMinutes(30);
    vehicle.setHealthScore(85);
    
    Map<ScoringDimension, Double> weights = Map.of(
        ScoringDimension.DISTANCE, 0.4,
        ScoringDimension.BATTERY, 0.3,
        ScoringDimension.IDLE_TIME, 0.2,
        ScoringDimension.HEALTH, 0.1
    );
    
    double totalScore = scoringService.calculateWeightedScore(vehicle, weights);
    assertTrue(totalScore >= 0 && totalScore <= 100);
}

@Test
void testNormalScoringStrategy() {
    // 测试普通时段评分策略
    ScoringStrategy strategy = strategyFactory.getStrategy(ScoringScene.NORMAL);
    assertNotNull(strategy);
    
    Vehicle vehicle = createTestVehicle();
    double score = strategy.calculateScore(vehicle);
    assertTrue(score >= 0 && score <= 100);
}

@Test
void testPeakHourScoringStrategy() {
    // 测试高峰时段评分策略
    ScoringStrategy strategy = strategyFactory.getStrategy(ScoringScene.PEAK_HOUR);
    assertNotNull(strategy);
    
    Vehicle vehicle = createTestVehicle();
    double score = strategy.calculateScore(vehicle);
    assertTrue(score >= 0 && score <= 100);
}

@Test
void testLowBatteryScoringStrategy() {
    // 测试低电量召回评分策略
    ScoringStrategy strategy = strategyFactory.getStrategy(ScoringScene.LOW_BATTERY);
    assertNotNull(strategy);
    
    Vehicle vehicle = createTestVehicle();
    double score = strategy.calculateScore(vehicle);
    assertTrue(score >= 0 && score <= 100);
}
```

#### 2. 边界测试用例
```java
@Test
void testDistanceScoreWithZeroDistance() {
    // 测试零距离评分
    double score = scoringService.calculateDistanceScore(0);
    assertEquals(100, score); // 零距离应该得满分
}

@Test
void testDistanceScoreWithMaxDistance() {
    // 测试最大距离评分
    double score = scoringService.calculateDistanceScore(100); // 100公里
    assertEquals(0, score); // 超远距离应该得0分
}

@Test
void testBatteryScoreWithZeroBattery() {
    // 测试零电量评分
    double score = scoringService.calculateBatteryScore(0);
    assertEquals(0, score); // 零电量应该得0分
}

@Test
void testBatteryScoreWithFullBattery() {
    // 测试满电量评分
    double score = scoringService.calculateBatteryScore(100);
    assertEquals(100, score); // 满电量应该得满分
}

@Test
void testWeightedScoreWithZeroWeights() {
    // 测试全零权重
    Map<ScoringDimension, Double> weights = Map.of(
        ScoringDimension.DISTANCE, 0.0,
        ScoringDimension.BATTERY, 0.0,
        ScoringDimension.IDLE_TIME, 0.0,
        ScoringDimension.HEALTH, 0.0
    );
    
    Vehicle vehicle = createTestVehicle();
    double score = scoringService.calculateWeightedScore(vehicle, weights);
    assertEquals(0, score); // 全零权重应该得0分
}

@Test
void testWeightedScoreWithNormalizedWeights() {
    // 测试权重归一化（权重和不为1时应自动归一化）
    Map<ScoringDimension, Double> weights = Map.of(
        ScoringDimension.DISTANCE, 2.0,
        ScoringDimension.BATTERY, 3.0,
        ScoringDimension.IDLE_TIME, 2.0,
        ScoringDimension.HEALTH, 3.0
    );
    
    Vehicle vehicle = createTestVehicle();
    double score = scoringService.calculateWeightedScore(vehicle, weights);
    assertTrue(score >= 0 && score <= 100);
}
```

#### 3. 失败测试用例
```java
@Test
void testDistanceScoreWithNegativeDistance() {
    // 测试负距离评分
    assertThrows(IllegalArgumentException.class, () -> {
        scoringService.calculateDistanceScore(-1);
    });
}

@Test
void testBatteryScoreWithInvalidBattery() {
    // 测试无效电量评分
    assertThrows(IllegalArgumentException.class, () -> {
        scoringService.calculateBatteryScore(150); // 超过100%
    });
    
    assertThrows(IllegalArgumentException.class, () -> {
        scoringService.calculateBatteryScore(-10); // 负数
    });
}

@Test
void testStrategyFactoryWithInvalidScene() {
    // 测试无效场景策略
    assertThrows(IllegalArgumentException.class, () -> {
        strategyFactory.getStrategy(null);
    });
}

@Test
void testScoringWithNullVehicle() {
    // 测试空车辆评分
    assertThrows(IllegalArgumentException.class, () -> {
        scoringService.calculateWeightedScore(null, Map.of());
    });
}

@Test
void testScoringWithMissingDimensions() {
    // 测试缺少评分维度
    Map<ScoringDimension, Double> weights = Map.of(
        ScoringDimension.DISTANCE, 1.0
        // 缺少其他维度
    );
    
    Vehicle vehicle = createTestVehicle();
    assertThrows(IllegalArgumentException.class, () -> {
        scoringService.calculateWeightedScore(vehicle, weights);
    });
}
```

### 集成测试用例

#### 1. 正常流程测试
```java
@Test
void testMapVehicleRecommendation() {
    // 测试地图车辆推荐接口
    double lat = 30.5;
    double lng = 114.3;
    double radius = 3.0; // 3公里
    int limit = 20;
    
    List<VehicleRecommendation> recommendations = 
        vehicleScoringService.recommendForMap(lat, lng, radius, limit);
    
    assertNotNull(recommendations);
    assertTrue(recommendations.size() <= limit);
    
    // 验证返回的车辆包含坐标信息
    recommendations.forEach(rec -> {
        assertNotNull(rec.getVehicle().getLatitude());
        assertNotNull(rec.getVehicle().getLongitude());
        assertNotNull(rec.getScore());
    });
    
    // 验证按评分降序排序
    for (int i = 0; i < recommendations.size() - 1; i++) {
        assertTrue(recommendations.get(i).getScore() >= 
                   recommendations.get(i + 1).getScore());
    }
}

@Test
void testListVehicleRecommendation() {
    // 测试车辆列表推荐接口
    double lat = 30.5;
    double lng = 114.3;
    int limit = 10;
    
    List<VehicleRecommendation> recommendations = 
        vehicleScoringService.recommendForList(lat, lng, limit);
    
    assertNotNull(recommendations);
    assertTrue(recommendations.size() <= limit);
    
    // 验证返回的车辆不包含坐标信息（或坐标为null）
    recommendations.forEach(rec -> {
        assertNull(rec.getVehicle().getLatitude());
        assertNull(rec.getVehicle().getLongitude());
        assertNotNull(rec.getScore());
        assertNotNull(rec.getVehicle().getVehicleNo());
        assertNotNull(rec.getVehicle().getBatteryLevel());
    });
}

@Test
void testDynamicWeightSwitching() {
    // 测试动态权重切换
    // 1. 使用普通策略
    Vehicle vehicle = createTestVehicle();
    ScoringStrategy normalStrategy = strategyFactory.getStrategy(ScoringScene.NORMAL);
    double normalScore = normalStrategy.calculateScore(vehicle);
    
    // 2. 切换到高峰策略
    ScoringStrategy peakStrategy = strategyFactory.getStrategy(ScoringScene.PEAK_HOUR);
    double peakScore = peakStrategy.calculateScore(vehicle);
    
    // 3. 验证分数可能不同（取决于权重配置）
    // 注意：如果权重相同，分数可能相同
    assertNotNull(normalScore);
    assertNotNull(peakScore);
}

@Test
void testRecommendationWithCaching() {
    // 测试推荐结果缓存
    double lat = 30.5;
    double lng = 114.3;
    int limit = 10;
    
    // 第一次调用
    long start1 = System.currentTimeMillis();
    List<VehicleRecommendation> recommendations1 = 
        vehicleScoringService.recommendForList(lat, lng, limit);
    long time1 = System.currentTimeMillis() - start1;
    
    // 第二次调用（应该命中缓存）
    long start2 = System.currentTimeMillis();
    List<VehicleRecommendation> recommendations2 = 
        vehicleScoringService.recommendForList(lat, lng, limit);
    long time2 = System.currentTimeMillis() - start2;
    
    // 验证结果相同
    assertEquals(recommendations1.size(), recommendations2.size());
    
    // 验证第二次调用更快（缓存命中）
    // 注意：这个断言可能不稳定，仅作参考
    assertTrue(time2 <= time1);
}
```

#### 2. 异常场景测试
```java
@Test
void testRecommendationWithNoVehicles() {
    // 测试无车辆推荐场景
    double lat = 90.0; // 极地，应该没有车辆
    double lng = 0.0;
    int limit = 10;
    
    List<VehicleRecommendation> recommendations = 
        vehicleScoringService.recommendForList(lat, lng, limit);
    
    assertNotNull(recommendations);
    assertTrue(recommendations.isEmpty());
}

@Test
void testRecommendationWithInvalidCoordinates() {
    // 测试无效坐标
    double lat = 100.0; // 超出范围
    double lng = 200.0; // 超出范围
    
    assertThrows(IllegalArgumentException.class, () -> {
        vehicleScoringService.recommendForList(lat, lng, 10);
    });
}

@Test
void testRecommendationWithZeroLimit() {
    // 测试零limit
    double lat = 30.5;
    double lng = 114.3;
    
    assertThrows(IllegalArgumentException.class, () -> {
        vehicleScoringService.recommendForList(lat, lng, 0);
    });
}

@Test
void testRecommendationWithNegativeLimit() {
    // 测试负数limit
    double lat = 30.5;
    double lng = 114.3;
    
    assertThrows(IllegalArgumentException.class, () -> {
        vehicleScoringService.recommendForList(lat, lng, -1);
    });
}
```

#### 3. 性能边界测试
```java
@Test
void testRecommendationPerformance() {
    // 测试推荐性能
    double lat = 30.5;
    double lng = 114.3;
    int limit = 50;
    
    long start = System.currentTimeMillis();
    List<VehicleRecommendation> recommendations = 
        vehicleScoringService.recommendForMap(lat, lng, 10.0, limit);
    long time = System.currentTimeMillis() - start;
    
    // 验证响应时间在可接受范围内（如500ms内）
    assertTrue(time < 500, "推荐接口响应时间过长: " + time + "ms");
}

@Test
void testConcurrentRecommendations() {
    // 测试并发推荐
    int threadCount = 10;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    
    for (int i = 0; i < threadCount; i++) {
        executor.submit(() -> {
            try {
                List<VehicleRecommendation> recommendations = 
                    vehicleScoringService.recommendForList(30.5, 114.3, 10);
                assertNotNull(recommendations);
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
- [ ] 评分引擎能正确计算各维度分数
- [ ] 多维度加权评分计算正确
- [ ] 策略模式支持不同场景评分
- [ ] 运行时可动态切换评分权重
- [ ] 地图区域车辆推荐接口返回带坐标的车辆列表
- [ ] 车辆列表推荐接口返回不含坐标的车辆信息
- [ ] 推荐结果按评分降序排序
- [ ] 支持分页查询
- [ ] 推荐结果有缓存机制
- [ ] 单元测试覆盖率 ≥ 80%
- [ ] 集成测试通过
- [ ] 性能满足要求（响应时间 < 500ms）

## 依赖项
- 车辆数据（Redis缓存或MySQL）
- 车辆位置数据（经纬度坐标）
- 车辆状态数据（电量、空闲时长、健康度）
- 评分权重配置

## 风险与注意事项
1. 评分算法需要根据业务需求调整权重
2. 距离计算需要考虑地球曲率（Haversine公式）
3. 缓存策略需要考虑数据一致性（车辆状态变化时清除缓存）
4. 性能优化需要考虑批量计算和并行处理
5. 策略模式需要支持热更新（不重启服务）