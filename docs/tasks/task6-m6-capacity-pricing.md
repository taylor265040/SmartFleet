# Task 6: M6 - 区域运力调度与动态定价

## 模块概述
实现GeoHash网格化（车辆位置编码、区域划分），实现网格供需比计算与tb_grid_dispatch数据维护，实现动态定价引擎（时段系数、区域密度、历史热度），实现运力调控建议生成（低供区域调度权重提升）。

---

## 子步骤

### 1. 工程准备
- [ ] 设计GeoHash网格化架构
- [ ] 设计动态定价引擎架构
- [ ] 创建网格运力实体类GridDispatch
- [ ] 创建定价规则实体类PricingRule
- [ ] 创建运力调度建议实体类DispatchSuggestion

### 2. 测试先行
- [ ] 编写GeoHash编码测试
- [ ] 编写GeoHash解码测试
- [ ] 编写网格划分测试
- [ ] 编写供需比计算测试
- [ ] 编写动态定价计算测试
- [ ] 编写运力调度建议生成测试

### 3. 硬编码跑通
- [ ] 实现GeoHash编码算法（硬编码精度和字符集）
- [ ] 实现GeoHash解码算法（硬编码边界计算）
- [ ] 实现网格划分逻辑（硬编码网格大小）
- [ ] 实现供需比计算（硬编码计算公式）
- [ ] 实现动态定价计算（硬编码定价规则）
- [ ] 验证GeoHash和定价计算可正常工作

### 4. 骨架
- [ ] 创建GeoHashService接口和实现类
- [ ] 创建GridDispatchService接口和实现类
- [ ] 创建DynamicPricingService接口和实现类
- [ ] 创建CapacityDispatchService接口和实现类
- [ ] 创建GridDispatchController（网格运力接口）
- [ ] 创建PricingController（定价接口）

### 5. 数据加载
- [ ] 实现GeoHash配置加载（精度、字符集）
- [ ] 实现网格运力数据加载（从tb_grid_dispatch）
- [ ] 实现定价规则数据加载（从数据库或配置文件）
- [ ] 实现历史热度数据加载（从数据库或日志）
- [ ] 创建定价规则配置表和实体类

### 6. 检索实现
- [ ] 实现网格运力查询接口（按区域查询供需比）
- [ ] 实现动态定价查询接口（按区域和时间查询价格）
- [ ] 实现运力调度建议查询接口
- [ ] 实现网格车辆分布查询接口
- [ ] 实现定价历史查询接口

### 7. 集成具体实现
- [ ] 集成GeoHash与车辆位置更新
- [ ] 集成网格运力与车辆调度引擎
- [ ] 集成动态定价与订单创建流程
- [ ] 实现运力调控建议与调度策略联动
- [ ] 实现网格运力数据定时更新
- [ ] 编写集成测试验证完整运力调度流程

---

## 测试用例记录 (test-record)

### 单元测试用例

#### 1. 正常测试用例
```java
@Test
void testGeoHashEncoding() {
    // 测试GeoHash编码
    double lat = 30.5;
    double lng = 114.3;
    int precision = 6;
    
    String geoHash = geoHashService.encode(lat, lng, precision);
    
    assertNotNull(geoHash);
    assertEquals(precision, geoHash.length());
    
    // 验证编码字符在有效范围内
    String base32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    for (char c : geoHash.toCharArray()) {
        assertTrue(base32.indexOf(c) >= 0);
    }
}

@Test
void testGeoHashDecoding() {
    // 测试GeoHash解码
    String geoHash = "w3w6v0";
    
    GeoHashPoint point = geoHashService.decode(geoHash);
    
    assertNotNull(point);
    assertNotNull(point.getLatitude());
    assertNotNull(point.getLongitude());
    
    // 验证解码精度
    assertTrue(Math.abs(point.getLatitude() - 30.5) < 0.01);
    assertTrue(Math.abs(point.getLongitude() - 114.3) < 0.01);
}

@Test
void testGeoHashNeighbors() {
    // 测试GeoHash邻居计算
    String geoHash = "w3w6v0";
    
    List<String> neighbors = geoHashService.getNeighbors(geoHash);
    
    assertNotNull(neighbors);
    assertEquals(8, neighbors.size()); // 8个邻居
    
    // 验证邻居长度与原始GeoHash相同
    neighbors.forEach(neighbor -> {
        assertEquals(geoHash.length(), neighbor.length());
    });
}

@Test
void testGrid划分() {
    // 测试网格划分
    double lat = 30.5;
    double lng = 114.3;
    double radius = 3.0; // 3公里
    
    List<String> grids = geoHashService.getGridsInRadius(lat, lng, radius);
    
    assertNotNull(grids);
    assertFalse(grids.isEmpty());
    
    // 验证网格覆盖区域
    grids.forEach(grid -> {
        GeoHashPoint point = geoHashService.decode(grid);
        double distance = calculateDistance(lat, lng, point.getLatitude(), point.getLongitude());
        assertTrue(distance <= radius + 0.5); // 允许一定误差
    });
}

@Test
void testSupplyDemandRatio() {
    // 测试供需比计算
    String gridId = "w3w6v0";
    
    // 模拟数据：10辆车，5个订单
    when(gridDispatchRepository.findByGridId(gridId)).thenReturn(
        new GridDispatch(gridId, 10, 5, LocalDateTime.now())
    );
    
    double ratio = gridDispatchService.calculateSupplyDemandRatio(gridId);
    
    assertEquals(2.0, ratio, 0.01); // 10/5 = 2.0
}

@Test
void testDynamicPricing() {
    // 测试动态定价
    String gridId = "w3w6v0";
    LocalDateTime time = LocalDateTime.now();
    
    // 模拟数据
    when(pricingRuleRepository.findByGridId(gridId)).thenReturn(
        new PricingRule(gridId, 1.0, 1.2, 1.5, 0.8)
    );
    
    double basePrice = 10.0; // 基础价格
    double dynamicPrice = dynamicPricingService.calculatePrice(gridId, time, basePrice);
    
    // 验证价格计算
    assertTrue(dynamicPrice > 0);
    // 根据时段系数验证
}

@Test
void testPeakHourPricing() {
    // 测试高峰时段定价
    String gridId = "w3w6v0";
    LocalDateTime peakTime = LocalDateTime.of(2024, 1, 1, 8, 0); // 早上8点
    
    double basePrice = 10.0;
    double peakPrice = dynamicPricingService.calculatePrice(gridId, peakTime, basePrice);
    
    // 高峰时段价格应该更高
    LocalDateTime offPeakTime = LocalDateTime.of(2024, 1, 1, 14, 0); // 下午2点
    double offPeakPrice = dynamicPricingService.calculatePrice(gridId, offPeakTime, basePrice);
    
    assertTrue(peakPrice > offPeakPrice);
}

@Test
void testLowSupplyAreaDetection() {
    // 测试低供区域检测
    String gridId = "w3w6v0";
    
    // 模拟低供数据：2辆车，10个订单
    when(gridDispatchRepository.findByGridId(gridId)).thenReturn(
        new GridDispatch(gridId, 2, 10, LocalDateTime.now())
    );
    
    boolean isLowSupply = gridDispatchService.isLowSupplyArea(gridId);
    assertTrue(isLowSupply);
}

@Test
void testDispatchSuggestionGeneration() {
    // 测试运力调度建议生成
    String gridId = "w3w6v0";
    
    // 模拟低供数据
    when(gridDispatchRepository.findByGridId(gridId)).thenReturn(
        new GridDispatch(gridId, 2, 10, LocalDateTime.now())
    );
    
    DispatchSuggestion suggestion = capacityDispatchService.generateSuggestion(gridId);
    
    assertNotNull(suggestion);
    assertNotNull(suggestion.getTargetGridId());
    assertNotNull(suggestion.getSuggestedVehicleCount());
    assertTrue(suggestion.getSuggestedVehicleCount() > 0);
}
```

#### 2. 边界测试用例
```java
@Test
void testGeoHashWithInvalidCoordinates() {
    // 测试无效坐标
    double lat = 100.0; // 超出范围
    double lng = 200.0; // 超出范围
    
    assertThrows(IllegalArgumentException.class, () -> {
        geoHashService.encode(lat, lng, 6);
    });
}

@Test
void testGeoHashWithInvalidPrecision() {
    // 测试无效精度
    double lat = 30.5;
    double lng = 114.3;
    
    assertThrows(IllegalArgumentException.class, () -> {
        geoHashService.encode(lat, lng, 0);
    });
    
    assertThrows(IllegalArgumentException.class, () -> {
        geoHashService.encode(lat, lng, 20);
    });
}

@Test
void testGeoHashWithInvalidHash() {
    // 测试无效GeoHash字符串
    assertThrows(IllegalArgumentException.class, () -> {
        geoHashService.decode("");
    });
    
    assertThrows(IllegalArgumentException.class, () -> {
        geoHashService.decode(null);
    });
    
    // 包含无效字符
    assertThrows(IllegalArgumentException.class, () -> {
        geoHashService.decode("abc123!");
    });
}

@Test
void testSupplyDemandRatioWithZeroVehicles() {
    // 测试零车辆供需比
    String gridId = "w3w6v0";
    
    when(gridDispatchRepository.findByGridId(gridId)).thenReturn(
        new GridDispatch(gridId, 0, 5, LocalDateTime.now())
    );
    
    double ratio = gridDispatchService.calculateSupplyDemandRatio(gridId);
    
    assertEquals(0.0, ratio, 0.01);
}

@Test
void testSupplyDemandRatioWithZeroOrders() {
    // 测试零订单供需比
    String gridId = "w3w6v0";
    
    when(gridDispatchRepository.findByGridId(gridId)).thenReturn(
        new GridDispatch(gridId, 10, 0, LocalDateTime.now())
    );
    
    double ratio = gridDispatchService.calculateSupplyDemandRatio(gridId);
    
    assertEquals(Double.POSITIVE_INFINITY, ratio, 0.01);
}

@Test
void testDynamicPricingWithExtremeDemand() {
    // 测试极端需求定价
    String gridId = "w3w6v0";
    LocalDateTime time = LocalDateTime.now();
    
    // 模拟极高需求
    when(pricingRuleRepository.findByGridId(gridId)).thenReturn(
        new PricingRule(gridId, 1.0, 3.0, 5.0, 0.5) // 极高系数
    );
    
    double basePrice = 10.0;
    double extremePrice = dynamicPricingService.calculatePrice(gridId, time, basePrice);
    
    // 验证价格有上限
    assertTrue(extremePrice <= basePrice * 5.0); // 假设最高5倍
}

@Test
void testDynamicPricingWithNegativeBasePrice() {
    // 测试负基础价格
    String gridId = "w3w6v0";
    LocalDateTime time = LocalDateTime.now();
    
    assertThrows(IllegalArgumentException.class, () -> {
        dynamicPricingService.calculatePrice(gridId, time, -10.0);
    });
}

@Test
void testDispatchSuggestionWithHighSupplyArea() {
    // 测试高供区域调度建议
    String gridId = "w3w6v0";
    
    // 模拟高供数据：20辆车，5个订单
    when(gridDispatchRepository.findByGridId(gridId)).thenReturn(
        new GridDispatch(gridId, 20, 5, LocalDateTime.now())
    );
    
    DispatchSuggestion suggestion = capacityDispatchService.generateSuggestion(gridId);
    
    // 高供区域应该没有调度建议
    assertNull(suggestion);
}
```

#### 3. 失败测试用例
```java
@Test
void testGeoHashEncodingWithNaN() {
    // 测试NaN坐标
    assertThrows(IllegalArgumentException.class, () -> {
        geoHashService.encode(Double.NaN, 114.3, 6);
    });
    
    assertThrows(IllegalArgumentException.class, () -> {
        geoHashService.encode(30.5, Double.NaN, 6);
    });
}

@Test
void testGeoHashEncodingWithInfinity() {
    // 测试无穷大坐标
    assertThrows(IllegalArgumentException.class, () -> {
        geoHashService.encode(Double.POSITIVE_INFINITY, 114.3, 6);
    });
    
    assertThrows(IllegalArgumentException.class, () -> {
        geoHashService.encode(30.5, Double.NEGATIVE_INFINITY, 6);
    });
}

@Test
void testGridDispatchWithDatabaseFailure() {
    // 测试数据库故障
    String gridId = "w3w6v0";
    
    when(gridDispatchRepository.findByGridId(gridId))
        .thenThrow(new DataAccessException("数据库故障") {});
    
    assertThrows(DataAccessException.class, () -> {
        gridDispatchService.calculateSupplyDemandRatio(gridId);
    });
}

@Test
void testDynamicPricingWithInvalidGridId() {
    // 测试无效网格ID
    String invalidGridId = "";
    LocalDateTime time = LocalDateTime.now();
    
    assertThrows(IllegalArgumentException.class, () -> {
        dynamicPricingService.calculatePrice(invalidGridId, time, 10.0);
    });
}

@Test
void testDynamicPricingWithNullTime() {
    // 测试空时间
    String gridId = "w3w6v0";
    
    assertThrows(IllegalArgumentException.class, () -> {
        dynamicPricingService.calculatePrice(gridId, null, 10.0);
    });
}
```

### 集成测试用例

#### 1. 正常流程测试
```java
@Test
void testFullCapacityDispatchFlow() {
    // 测试完整运力调度流程
    // 1. 更新车辆位置（触发GeoHash计算）
    Long vehicleId = 1L;
    double lat = 30.5;
    double lng = 114.3;
    
    vehicleLocationService.updateLocation(vehicleId, lat, lng);
    
    // 2. 查询车辆所在网格
    String gridId = geoHashService.encode(lat, lng, 6);
    
    // 3. 更新网格运力数据
    gridDispatchService.updateGridCapacity(gridId);
    
    // 4. 查询网格供需比
    double ratio = gridDispatchService.calculateSupplyDemandRatio(gridId);
    assertTrue(ratio > 0);
    
    // 5. 计算动态价格
    double basePrice = 10.0;
    double dynamicPrice = dynamicPricingService.calculatePrice(gridId, LocalDateTime.now(), basePrice);
    assertTrue(dynamicPrice > 0);
    
    // 6. 生成运力调度建议
    DispatchSuggestion suggestion = capacityDispatchService.generateSuggestion(gridId);
    // 根据供需比验证是否有建议
}

@Test
void testGridCapacityUpdate() {
    // 测试网格运力更新
    String gridId = "w3w6v0";
    
    // 模拟网格内有车辆
    when(vehicleRepository.findByGridId(gridId)).thenReturn(
        Arrays.asList(
            createVehicle(1L, VehicleStatus.AVAILABLE),
            createVehicle(2L, VehicleStatus.AVAILABLE),
            createVehicle(3L, VehicleStatus.RENTING)
        )
    );
    
    // 模拟网格内有订单
    when(orderRepository.findByGridId(gridId)).thenReturn(
        Arrays.asList(
            createOrder(1L, OrderStatus.RUNNING),
            createOrder(2L, OrderStatus.CREATED)
        )
    );
    
    // 更新网格运力
    gridDispatchService.updateGridCapacity(gridId);
    
    // 验证更新结果
    GridDispatch gridDispatch = gridDispatchRepository.findByGridId(gridId);
    assertEquals(3, gridDispatch.getVehicleCount()); // 总车辆数
    assertEquals(2, gridDispatch.getOrderCount()); // 总订单数
    assertEquals(2, gridDispatch.getAvailableVehicleCount()); // 可用车辆数
}

@Test
void testDynamicPricingWithTimeVariation() {
    // 测试动态定价时间变化
    String gridId = "w3w6v0";
    double basePrice = 10.0;
    
    // 测试不同时段价格
    LocalDateTime morning = LocalDateTime.of(2024, 1, 1, 8, 0); // 早高峰
    LocalDateTime afternoon = LocalDateTime.of(2024, 1, 1, 14, 0); // 下午
    LocalDateTime evening = LocalDateTime.of(2024, 1, 1, 18, 0); // 晚高峰
    LocalDateTime night = LocalDateTime.of(2024, 1, 1, 23, 0); // 深夜
    
    double morningPrice = dynamicPricingService.calculatePrice(gridId, morning, basePrice);
    double afternoonPrice = dynamicPricingService.calculatePrice(gridId, afternoon, basePrice);
    double eveningPrice = dynamicPricingService.calculatePrice(gridId, evening, basePrice);
    double nightPrice = dynamicPricingService.calculatePrice(gridId, night, basePrice);
    
    // 验证高峰时段价格更高
    assertTrue(morningPrice > afternoonPrice);
    assertTrue(eveningPrice > afternoonPrice);
    
    // 验证深夜价格可能更低
    assertTrue(nightPrice <= afternoonPrice);
}

@Test
void testCapacityDispatchWithMultipleGrids() {
    // 测试多网格运力调度
    String grid1 = "w3w6v0";
    String grid2 = "w3w6v1";
    String grid3 = "w3w6v2";
    
    // 模拟不同供需比
    when(gridDispatchRepository.findByGridId(grid1)).thenReturn(
        new GridDispatch(grid1, 2, 10, LocalDateTime.now()) // 低供
    );
    when(gridDispatchRepository.findByGridId(grid2)).thenReturn(
        new GridDispatch(grid2, 10, 10, LocalDateTime.now()) // 平衡
    );
    when(gridDispatchRepository.findByGridId(grid3)).thenReturn(
        new GridDispatch(grid3, 20, 5, LocalDateTime.now()) // 高供
    );
    
    // 生成调度建议
    List<DispatchSuggestion> suggestions = capacityDispatchService.generateSuggestionsForArea(
        Arrays.asList(grid1, grid2, grid3)
    );
    
    // 只有低供区域应该有建议
    assertNotNull(suggestions);
    assertEquals(1, suggestions.size());
    assertEquals(grid1, suggestions.get(0).getTargetGridId());
}
```

#### 2. 异常场景测试
```java
@Test
void testGridCapacityUpdateWithNoVehicles() {
    // 测试无车辆时的网格运力更新
    String gridId = "w3w6v0";
    
    when(vehicleRepository.findByGridId(gridId)).thenReturn(Collections.emptyList());
    when(orderRepository.findByGridId(gridId)).thenReturn(Collections.emptyList());
    
    // 更新网格运力
    gridDispatchService.updateGridCapacity(gridId);
    
    // 验证更新结果
    GridDispatch gridDispatch = gridDispatchRepository.findByGridId(gridId);
    assertEquals(0, gridDispatch.getVehicleCount());
    assertEquals(0, gridDispatch.getOrderCount());
    assertEquals(0, gridDispatch.getAvailableVehicleCount());
}

@Test
void testDynamicPricingWithInvalidGrid() {
    // 测试无效网格的动态定价
    String invalidGridId = "invalid_grid";
    LocalDateTime time = LocalDateTime.now();
    
    when(pricingRuleRepository.findByGridId(invalidGridId)).thenReturn(null);
    
    // 应该使用默认定价规则
    double basePrice = 10.0;
    double price = dynamicPricingService.calculatePrice(invalidGridId, time, basePrice);
    
    // 验证使用默认价格
    assertEquals(basePrice, price, 0.01);
}

@Test
void testCapacityDispatchWithDatabaseFailure() {
    // 测试数据库故障时的运力调度
    String gridId = "w3w6v0";
    
    when(gridDispatchRepository.findByGridId(gridId))
        .thenThrow(new DataAccessException("数据库故障") {});
    
    // 应该降级处理
    DispatchSuggestion suggestion = capacityDispatchService.generateSuggestion(gridId);
    
    // 验证降级结果
    assertNull(suggestion); // 或返回默认建议
}

@Test
void testGeoHashWithEdgeCoordinates() {
    // 测试边界坐标
    // 测试南极点
    String southPole = geoHashService.encode(-90.0, 0.0, 6);
    assertNotNull(southPole);
    
    // 测试北极点
    String northPole = geoHashService.encode(90.0, 0.0, 6);
    assertNotNull(northPole);
    
    // 测试本初子午线
    String primeMeridian = geoHashService.encode(0.0, 0.0, 6);
    assertNotNull(primeMeridian);
    
    // 测试国际日期变更线
    String dateLine = geoHashService.encode(0.0, 180.0, 6);
    assertNotNull(dateLine);
}
```

#### 3. 性能边界测试
```java
@Test
void testGeoHashEncodingPerformance() {
    // 测试GeoHash编码性能
    int iterations = 10000;
    double lat = 30.5;
    double lng = 114.3;
    
    long start = System.currentTimeMillis();
    
    for (int i = 0; i < iterations; i++) {
        geoHashService.encode(lat, lng, 6);
    }
    
    long time = System.currentTimeMillis() - start;
    
    // 验证性能在可接受范围内（如10000次操作在1秒内）
    assertTrue(time < 1000, "GeoHash编码性能过低: " + time + "ms for " + iterations + " operations");
}

@Test
void testGridCapacityUpdatePerformance() {
    // 测试网格运力更新性能
    String gridId = "w3w6v0";
    int iterations = 100;
    
    when(vehicleRepository.findByGridId(gridId)).thenReturn(
        Collections.nCopies(100, createVehicle(1L, VehicleStatus.AVAILABLE))
    );
    
    long start = System.currentTimeMillis();
    
    for (int i = 0; i < iterations; i++) {
        gridDispatchService.updateGridCapacity(gridId);
    }
    
    long time = System.currentTimeMillis() - start;
    
    // 验证性能在可接受范围内（如100次操作在2秒内）
    assertTrue(time < 2000, "网格运力更新性能过低: " + time + "ms for " + iterations + " operations");
}

@Test
void testConcurrentDynamicPricing() {
    // 测试并发动态定价
    String gridId = "w3w6v0";
    int threadCount = 100;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    
    for (int i = 0; i < threadCount; i++) {
        executor.submit(() -> {
            try {
                double price = dynamicPricingService.calculatePrice(gridId, LocalDateTime.now(), 10.0);
                assertTrue(price > 0);
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
- [ ] GeoHash编码/解码功能正常
- [ ] 网格划分功能正常
- [ ] 网格供需比计算正常
- [ ] 动态定价引擎正常工作（时段系数、区域密度、历史热度）
- [ ] 运力调度建议生成正常
- [ ] 网格运力数据维护正常
- [ ] 单元测试覆盖率 ≥ 80%
- [ ] 集成测试通过

## 依赖项
- 车辆位置数据
- 订单数据
- 定价规则配置
- 历史热度数据

## 风险与注意事项
1. GeoHash精度需要根据业务需求调整
2. 供需比计算需要考虑实时性（定时更新或事件驱动）
3. 动态定价需要考虑公平性（避免价格过高）
4. 运力调度建议需要考虑可行性（车辆可调度性）
5. 需要考虑边界情况（网格边缘、坐标精度）