# SmartFleet 测试用例记录文档

本文档记录SmartFleet智能车辆租赁管理平台各模块的测试用例，按照统一格式便于测试Agent执行。

**格式说明：**
- 测试用例：测试用例名称
- 输入：测试输入数据
- 预期输出：预期的执行结果
- 实际输出：实际执行结果（待填写）

---

## 模块1：M1 - 基础设施搭建

### 1.1 Spring Boot启动测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| Spring容器正常启动 | 启动SpringBoot应用 | ApplicationContext不为null，应用启动成功 | |
| MySQL连接正常 | 数据源配置正确 | 能成功获取数据库连接 | |
| Redis连接正常 | Redis配置正确 | RedisTemplate可正常操作，set/get操作成功 | |
| RocketMQ连接正常 | RocketMQ配置正确 | Producer不为null，可正常发送消息 | |

### 1.2 异常场景测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 数据库连接超时 | 模拟网络延迟或数据库不可用 | 连接超时异常，系统记录错误日志 | |
| Redis连接失败 | Redis服务不可用 | Redis操作失败，系统降级运行不崩溃 | |
| RocketMQ Broker不可达 | Broker地址错误或不可用 | 消息发送失败，触发重试机制 | |
| 无效数据库配置 | 错误的数据库连接参数 | 启动失败，抛出DataSourceBeanCreationException | |
| 无效Redis配置 | 错误的Redis连接参数 | Redis操作失败，系统不崩溃 | |

### 1.3 数据库表创建测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 表结构创建成功 | 执行建表SQL | tb_vehicle、tb_order、tb_grid_dispatch、tb_user表创建成功 | |
| 表字段完整 | 查询表结构 | 所有必需字段存在且类型正确 | |
| 主键和索引创建 | 查询索引信息 | 主键和必要索引创建成功 | |

---

## 模块2：M2 - 用户认证与安全

### 2.1 用户注册测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 正常注册 | username="testuser", password="password123", phone="13800138000" | 注册成功，返回用户对象，密码已加密存储 | |
| 重复用户名注册 | 已存在的username | 抛出BusinessException，提示用户名已存在 | |
| 无效手机号注册 | phone="invalidphone" | 抛出BusinessException，提示手机号格式错误 | |
| 弱密码注册 | password="123" | 抛出BusinessException，提示密码强度不足 | |
| 空用户名注册 | username=null | 抛出IllegalArgumentException | |
| 空密码注册 | password=null | 抛出IllegalArgumentException | |

### 2.2 用户登录测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 正常登录 | username="testuser", password="password123" | 登录成功，返回有效JWT Token | |
| 错误密码登录 | username="testuser", password="wrongpassword" | 抛出BusinessException，提示密码错误 | |
| 不存在用户登录 | username="nonexistent", password="password123" | 抛出BusinessException，提示用户不存在 | |
| 空用户名登录 | username=null | 抛出IllegalArgumentException | |
| 空密码登录 | password=null | 抛出IllegalArgumentException | |

### 2.3 JWT Token测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| Token生成 | User对象(id=1, username="testuser", role="ROLE_USER") | 生成有效JWT Token，长度大于0 | |
| Token解析 | 有效Token | 正确解析出username="testuser" | |
| Token验证 | Token + User对象 | 验证通过，返回true | |
| 过期Token验证 | 已过期的Token | 验证失败，返回false | |
| 无效Token验证 | "invalid.token.here" | 抛出JwtException | |
| Token刷新 | 旧Token | 生成新Token，新旧Token不同，username正确 | |
| 过期Token刷新 | 已过期的Token | 抛出BusinessException | |

### 2.4 权限控制测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 未授权访问 | 无Token访问受保护接口 | 返回401 Unauthorized | |
| 普通用户访问管理员接口 | 普通用户Token + /api/admin/users | 返回403 Forbidden | |
| 管理员访问管理员接口 | 管理员Token + /api/admin/users | 返回200 OK | |
| 使用过期Token访问 | 过期Token + 受保护接口 | 返回401 Unauthorized | |
| 使用无效Token访问 | 无效Token + 受保护接口 | 返回401 Unauthorized | |

---

## 模块3：M3 - 智能车辆调度引擎

### 3.1 评分计算测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 距离评分计算 | distance=2.5km | 返回0-100之间的分数，距离越近分数越高 | |
| 零距离评分 | distance=0km | 返回100分 | |
| 最大距离评分 | distance=100km | 返回0分 | |
| 负距离评分 | distance=-1km | 抛出IllegalArgumentException | |
| 电量评分计算 | batteryLevel=80% | 返回0-100之间的分数，电量越高分数越高 | |
| 零电量评分 | batteryLevel=0% | 返回0分 | |
| 满电量评分 | batteryLevel=100% | 返回100分 | |
| 无效电量评分 | batteryLevel=150% | 抛出IllegalArgumentException | |
| 空闲时长评分 | idleMinutes=30 | 返回0-100之间的分数 | |
| 健康度评分 | healthScore=85 | 返回0-100之间的分数 | |

### 3.2 多维度加权评分测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 正常加权评分 | Vehicle对象 + 权重Map{DISTANCE:0.4, BATTERY:0.3, IDLE_TIME:0.2, HEALTH:0.1} | 返回0-100之间的加权分数 | |
| 全零权重 | 权重Map所有维度为0 | 返回0分 | |
| 权重归一化 | 权重和不为1（如2,3,2,3） | 自动归一化，返回0-100之间的分数 | |
| 空车辆评分 | null Vehicle | 抛出IllegalArgumentException | |
| 缺少评分维度 | 权重Map缺少某些维度 | 抛出IllegalArgumentException | |

### 3.3 策略模式测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 普通时段策略 | ScoringScene.NORMAL | 返回策略实例，计算分数在0-100之间 | |
| 高峰时段策略 | ScoringScene.PEAK_HOUR | 返回策略实例，计算分数在0-100之间 | |
| 低电量召回策略 | ScoringScene.LOW_BATTERY | 返回策略实例，计算分数在0-100之间 | |
| 无效场景策略 | null ScoringScene | 抛出IllegalArgumentException | |

### 3.4 车辆推荐接口测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 地图车辆推荐 | lat=30.5, lng=114.3, radius=3.0, limit=20 | 返回带坐标的车辆列表，数量≤20，按评分降序排序 | |
| 列表车辆推荐 | lat=30.5, lng=114.3, limit=10 | 返回不含坐标的车辆列表，数量≤10，按评分降序排序 | |
| 无车辆推荐 | lat=90.0, lng=0.0, limit=10 | 返回空数组 | |
| 无效坐标 | lat=100.0, lng=200.0 | 抛出IllegalArgumentException | |
| 零limit | limit=0 | 抛出IllegalArgumentException | |
| 负数limit | limit=-1 | 抛出IllegalArgumentException | |
| 推荐结果缓存 | 相同参数调用两次 | 第二次调用更快（缓存命中） | |

### 3.5 性能测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 推荐接口性能 | lat=30.5, lng=114.3, radius=10.0, limit=50 | 响应时间<500ms | |
| 并发推荐 | 10线程同时调用推荐接口 | 所有线程成功返回，无异常 | |

---

## 模块4：M4 - 高并发租赁一致性控制

### 4.1 Redis预占锁测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 获取锁成功 | vehicleId=1, userId=100, lockSeconds=300 | 返回true，锁存在，持有者为userId=100 | |
| 释放锁成功 | vehicleId=1, userId=100 | 返回true，锁不存在 | |
| 锁过期 | vehicleId=1, userId=100, lockSeconds=1 | 等待1秒后锁过期，其他用户可获取 | |
| 错误持有者释放锁 | vehicleId=1, wrongUserId=200 | 返回false，锁仍然存在 | |
| 空vehicleId | vehicleId=null | 抛出IllegalArgumentException | |
| 空userId | userId=null | 抛出IllegalArgumentException | |
| 负数lockSeconds | lockSeconds=-1 | 抛出IllegalArgumentException | |

### 4.2 MySQL乐观锁测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 乐观锁更新成功 | Order(version=1) -> status=CONFIRMED | 更新成功，version变为2 | |
| 过时version更新 | Order(version=1)但数据库version=2 | 更新失败，返回false | |

### 4.3 订单创建测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 正常创建订单 | userId=100, vehicleId=1, lat=30.5, lng=114.3 | 订单创建成功，状态为CREATED | |
| 无效车辆创建订单 | userId=100, vehicleId=999(不存在) | 抛出BusinessException | |
| 不可用车辆创建订单 | userId=100, vehicleId=1(status=RENTING) | 抛出BusinessException | |
| 空userId | userId=null | 抛出IllegalArgumentException | |
| 空vehicleId | vehicleId=null | 抛出IllegalArgumentException | |

### 4.4 订单确认测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 正常确认订单 | orderId, userId | 订单状态变为RUNNING | |
| 错误用户确认 | orderId, wrongUserId | 抛出BusinessException | |
| 预占锁过期确认 | 预占锁已过期 | 抛出BusinessException | |
| 车辆状态变更后确认 | 车辆已标记为维修 | 抛出BusinessException，订单自动取消 | |

### 4.5 订单完成/取消测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 正常完成订单 | orderId, userId | 订单状态变为COMPLETED | |
| 正常取消订单 | orderId, userId | 订单状态变为CANCELLED | |
| 错误状态取消 | 已确认的订单尝试取消 | 抛出BusinessException | |

### 4.6 自旋重试测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 重试成功 | 前2次失败，第3次成功 | 执行3次，返回成功结果 | |
| 达到最大重试次数 | 连续失败3次 | 抛出ConcurrentModificationException | |

### 4.7 一键租赁测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 正常一键租赁 | userId=100, lat=30.5, lng=114.3 | 返回订单对象，状态为CREATED | |
| 无车辆可租 | lat=90.0, lng=0.0 | 抛出BusinessException | |
| 并发抢车 | 第一辆车被抢走 | 自动尝试第二辆车 | |

### 4.8 并发测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 并发创建订单 | 10线程同时创建同一辆车的订单 | 只有1个成功，其余失败 | |
| 高并发创建订单 | 100线程，10辆车 | 每辆车最多1个订单，无超卖 | |
| 锁性能测试 | 1000次锁操作 | 总时间<1000ms | |

### 4.9 异常场景测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| Redis故障降级 | Redis不可用 | 降级为仅MySQL乐观锁 | |
| 车辆状态变更 | 预占期间车辆标记为维修 | 订单自动取消 | |
| 订单超时自动取消 | 创建时间超过5分钟 | 订单自动取消，车辆释放 | |

---

## 模块5：M5 - 车辆状态机管理

### 5.1 状态机初始化测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 状态机初始化 | 无 | 初始化成功，当前状态为AVAILABLE | |
| 空状态初始化 | null | 抛出IllegalArgumentException | |

### 5.2 合法状态转移测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| AVAILABLE -> RESERVED | StateEvent.RESERVE | 转移成功，当前状态为RESERVED | |
| RESERVED -> RENTING | StateEvent.START_RENT | 转移成功，当前状态为RENTING | |
| RENTING -> AVAILABLE | StateEvent.END_RENT | 转移成功，当前状态为AVAILABLE | |
| AVAILABLE -> CHARGING | StateEvent.START_CHARGE | 转移成功，当前状态为CHARGING | |
| CHARGING -> AVAILABLE | StateEvent.END_CHARGE | 转移成功，当前状态为AVAILABLE | |
| AVAILABLE -> REPAIRING | StateEvent.START_REPAIR | 转移成功，当前状态为REPAIRING | |
| REPAIRING -> AVAILABLE | StateEvent.END_REPAIR | 转移成功，当前状态为AVAILABLE | |
| RESERVED -> AVAILABLE | StateEvent.CANCEL_RESERVE | 转移成功，当前状态为AVAILABLE | |

### 5.3 非法状态转移测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| AVAILABLE -> RENTING | StateEvent.START_RENT | 转移失败，状态不变 | |
| RENTING -> RESERVED | StateEvent.RESERVE | 转移失败，状态不变 | |
| CHARGING -> RENTING | StateEvent.START_RENT | 转移失败，状态不变 | |
| REPAIRING -> RENTING | StateEvent.START_RENT | 转移失败，状态不变 | |
| 空事件 | null StateEvent | 抛出IllegalArgumentException | |

### 5.4 状态转移校验测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 合法转移校验 | from=AVAILABLE, to=RESERVED | 返回true | |
| 非法转移校验 | from=AVAILABLE, to=RENTING | 返回false | |

### 5.5 Redis缓存同步测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 状态变更缓存同步 | 状态从AVAILABLE变为RESERVED | Redis缓存中状态更新为RESERVED | |

### 5.6 完整生命周期测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 完整车辆生命周期 | AVAILABLE->RESERVED->RENTING->AVAILABLE->CHARGING->AVAILABLE->REPAIRING->AVAILABLE | 所有状态转移成功 | |

### 5.7 异常场景测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 并发状态变更 | 10线程同时执行状态变更 | 只有1个成功，其余失败 | |
| 车辆不存在 | nonExistentVehicleId=999 | 抛出BusinessException | |
| 数据库故障 | 数据库不可用 | 抛出DataAccessException | |
| Redis故障 | Redis不可用 | 状态转移仍成功（降级处理） | |

### 5.8 性能测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 状态变更性能 | 1000次状态变更 | 总时间<2000ms | |
| 并发状态查询 | 100线程同时查询状态 | 所有查询成功返回 | |

---

## 模块6：M6 - 区域运力调度与动态定价

### 6.1 GeoHash编码测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 正常编码 | lat=30.5, lng=114.3, precision=6 | 返回6位GeoHash字符串 | |
| 无效坐标 | lat=100.0, lng=200.0 | 抛出IllegalArgumentException | |
| 无效精度 | precision=0 | 抛出IllegalArgumentException | |
| NaN坐标 | lat=NaN | 抛出IllegalArgumentException | |
| 无穷大坐标 | lat=Infinity | 抛出IllegalArgumentException | |

### 6.2 GeoHash解码测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 正常解码 | geoHash="w3w6v0" | 返回GeoHashPoint，lat≈30.5, lng≈114.3 | |
| 空字符串 | geoHash="" | 抛出IllegalArgumentException | |
| null值 | geoHash=null | 抛出IllegalArgumentException | |
| 无效字符 | geoHash="abc123!" | 抛出IllegalArgumentException | |

### 6.3 GeoHash邻居测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 获取邻居 | geoHash="w3w6v0" | 返回8个邻居，长度与原始GeoHash相同 | |

### 6.4 网格划分测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 半径内网格 | lat=30.5, lng=114.3, radius=3.0 | 返回网格列表，覆盖区域在radius+0.5范围内 | |

### 6.5 供需比计算测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 正常供需比 | vehicleCount=10, orderCount=5 | 返回2.0 | |
| 零车辆 | vehicleCount=0, orderCount=5 | 返回0.0 | |
| 零订单 | vehicleCount=10, orderCount=0 | 返回Infinity | |

### 6.6 动态定价测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 正常定价 | gridId, time, basePrice=10.0 | 返回>0的价格 | |
| 高峰时段定价 | morning=8:00, afternoon=14:00 | 早高峰价格>下午价格 | |
| 深夜定价 | night=23:00, afternoon=14:00 | 深夜价格≤下午价格 | |
| 负基础价格 | basePrice=-10.0 | 抛出IllegalArgumentException | |
| 极端需求定价 | 极高系数 | 价格有上限（≤basePrice*5.0） | |

### 6.7 低供区域检测测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 低供区域 | vehicleCount=2, orderCount=10 | 返回true | |
| 高供区域 | vehicleCount=20, orderCount=5 | 返回false | |

### 6.8 运力调度建议测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 低供区域建议 | vehicleCount=2, orderCount=10 | 返回建议对象，建议数量>0 | |
| 高供区域建议 | vehicleCount=20, orderCount=5 | 返回null | |
| 多网格建议 | [grid1(低供), grid2(平衡), grid3(高供)] | 只有grid1有建议 | |

### 6.9 边界坐标测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 南极点 | lat=-90.0, lng=0.0 | 返回有效GeoHash | |
| 北极点 | lat=90.0, lng=0.0 | 返回有效GeoHash | |
| 本初子午线 | lat=0.0, lng=0.0 | 返回有效GeoHash | |
| 国际日期变更线 | lat=0.0, lng=180.0 | 返回有效GeoHash | |

### 6.10 性能测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| GeoHash编码性能 | 10000次编码 | 总时间<1000ms | |
| 网格运力更新性能 | 100次更新 | 总时间<2000ms | |
| 并发动态定价 | 100线程同时计算价格 | 所有计算成功返回 | |

---

## 模块7：M7 - 实时数据处理与监控

### 7.1 Cache Aside读测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 缓存为空读取 | vehicleId=1, Redis缓存为空 | 从数据库加载，结果缓存到Redis | |
| 从缓存读取 | vehicleId=1, Redis缓存有数据 | 直接从缓存返回，不查询数据库 | |
| Redis故障读取 | Redis不可用 | 降级到数据库查询 | |
| 数据库故障读取 | 数据库不可用 | 抛出DataAccessException | |
| 空值缓存 | vehicleId=999, 数据库返回null | 缓存null值（防穿透） | |

### 7.2 Cache Aside写测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 正常更新 | Vehicle对象 | 数据库更新，缓存更新 | |
| 正常删除 | vehicleId | 数据库删除，缓存删除 | |

### 7.3 RocketMQ消息发送测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 正常发送 | topic="test", message="data" | 消息发送成功 | |
| 延迟发送 | topic="test", message="data", delayLevel=3 | 延迟消息发送成功 | |
| 空消息 | message=null | 抛出IllegalArgumentException | |
| Broker故障 | Broker不可用 | 抛出MQClientException | |

### 7.4 RocketMQ消息消费测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 正常消费 | MessageExt对象 | 业务逻辑被调用 | |
| 消费失败 | 无效消息数据 | 重试或进入死信队列 | |

### 7.5 WebSocket连接测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 建立连接 | WebSocketSession | 连接成功，session被管理 | |
| 断开连接 | WebSocketSession | 连接断开，session被移除 | |
| 连接状态查询 | sessionId | 返回true/false | |

### 7.6 WebSocket消息测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 发送消息 | session + message | 消息发送成功 | |
| 已关闭session发送 | closedSession + message | 跳过发送 | |
| 广播消息 | message | 所有活跃session收到消息 | |
| 网络故障发送 | session抛出IOException | 异常处理，连接标记为无效 | |

### 7.7 监控数据查询测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| 正常查询 | 无 | 返回监控数据（车辆数、订单数等） | |
| 无数据查询 | 数据库无数据 | 返回默认值（0） | |
| 部分数据故障 | 订单数据查询失败 | 返回部分数据，故障部分为默认值 | |

### 7.8 性能测试

| 测试用例 | 输入 | 预期输出 | 实际输出 |
|:---------|:-----|:---------|:---------|
| Cache Aside性能 | 10000次读取 | 总时间<1000ms | |
| RocketMQ性能 | 1000次发送 | 总时间<2000ms | |
| WebSocket广播性能 | 100会话100消息 | 总时间<5000ms | |
| 高并发WebSocket | 1000会话广播 | 广播成功，性能在可接受范围内 | |

---

## 测试执行说明

### 测试环境要求
- Java 17+
- MySQL 8.0+
- Redis 6.0+
- RocketMQ 5.0+

### 测试执行顺序
1. 先执行M1基础设施测试
2. 按模块依赖顺序执行：M1 → M2 → M3 → M4 → M5 → M6 → M7
3. 每个模块内按功能分组执行

### 测试结果记录
- 执行测试后，将实际输出填入"实际输出"列
- 标记测试通过/失败状态
- 记录失败原因和修复建议

### 测试覆盖率要求
- 各Service核心方法覆盖率 ≥ 80%
- 状态机所有合法/非法跃迁路径均有测试用例
- 评分引擎各策略权重计算有测试验证
- 一键租赁的自动重试逻辑有测试覆盖