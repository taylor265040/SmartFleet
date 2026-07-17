SmartFleet：智能分配车辆租赁管理平台

高并发车辆调度与实时运力优化系统

SmartFleet
是一个面向共享出行场景的智能车辆租赁与运力调度平台，涵盖车辆推荐、预占、租赁订单、动态定价、区域网格化调度及实时监控等业务。系统围绕有限车辆资源构建“推荐-预占-租赁”闭环链路，旨在提升车辆利用率与区域运力均衡度。

1. 项目简介

在共享单车/电单车及汽车租赁等高频出行场景中，如何合理分配车辆资源、应对高并发抢占、保障状态一致性并实现区域运力自适应平衡是系统的核心挑战。

SmartFleet 针对上述挑战，设计并实现了以下技术方案：

  - 多维车辆评分引擎：基于策略模式动态调整权重，实现差异化推荐。
  - 双重校验一致性控制：利用 Redis Lua 脚本预占锁结合 MySQL 乐观锁，配合自旋重试机制，应对高并发下的超卖与状态覆盖。
  - 异步落库与状态机：通过 RocketMQ 异步消峰及状态机模式，规范车辆生命周期流转。
  - 网格化运力调控：引入 GeoHash 算法进行区域供需比计算，实现动态定价与调度建议生成。

2. 技术栈

  - 核心框架：Spring Boot, MyBatis-Plus
  - 数据库：MySQL 8.0
  - 缓存与中间件：Redis, RocketMQ
  - 安全与认证：Spring Security, JWT
  - 推送与可视化：WebSocket, ECharts
  - 核心算法/设计模式：GeoHash 算法, 策略模式, 状态机模式, Redis Lua 脚本

3. 项目功能模块

SmartFleet
├── 车辆推荐与调度引擎 (策略模式多维评分)
├── 高并发租赁控制 (Redis Lua 预占 + 乐观锁 + 自旋重试)
├── 车辆状态机管理 (可租/预占/租赁中/充电/维修)
├── 区域运力与动态定价 (GeoHash 划分 + 供需比计算)
└── 实时运营监控 (Cache Aside + RocketMQ + WebSocket + ECharts)

3.1 智能车辆调度引擎

  - 多维加权评分：综合车辆与用户的距离、剩余电量、空闲时长及车辆健康度等指标进行实时评分。
  - 策略模式解耦：将不同业务场景（如普通时段、高峰时段、低电量优先召回等）抽象为不同的评分策略，支持运行时动态切换各指标的权重比。

3.2 高并发租赁一致性控制

  - 双重校验机制：
    1.  Redis 预占锁：通过 Lua 脚本执行原子性操作，校验车辆状态并锁定资源。
    2.  MySQL 乐观锁：在数据库写入阶段通过 version 字段进行二次校验，避免并发下的状态覆写。
  - 自旋重试（Spin Retry）：对于在 Redis
    阶段因抢占冲突而失败的请求，系统自动触发自旋重试，重新进行车辆评分与二次分配，缓解用户请求被直接拒绝的现象。

3.3 车辆生命周期状态管理

  - 状态机模式：定义车辆状态机（包含：可租 AVAILABLE、预占 RESERVED、租赁中 RENTING、充电中 CHARGING、维修中
    REPAIRING 等）。
  - 流转校验：严格限制非法状态转移（例如：禁止直接从“租赁中”跳转到“可租”，必须经过“结算/还车”或“维护检查”）。
  - Redis 同步：状态变更同步更新 Redis 缓存，保证查询与校验的实时性。

3.4 区域运力调度与动态定价

  - GeoHash 网格化：利用 GeoHash 将服务区域划分为不同精度的网格，实时统计各网格内的空闲车辆数与订单需求数。
  - 运力调控：当某网格供需比低于设定阈值时，自动提高该区域的调度权重，向调度端推送车辆调入建议。
  - 动态定价：结合时段系数、区域车辆密度与历史订单热度，动态微调起步价或里程费率，利用价格杠杆调节用户还车意向。

3.5 实时数据处理与运营监控

  - Cache Aside 读写模式：高频读取车辆状态时优先走 Redis 缓存，缓存失效时回源数据库。
  - 异步削峰：车辆位置更新、里程上报及订单状态落库等高频写操作，通过 RocketMQ 异步投递，减轻 MySQL 写入压力。
  - 大屏实时推送：后端采用 WebSocket 定时/按需推送各区域车辆热力分布及核心业务指标，前端结合 ECharts 实现运营大屏动态渲染。

4. 数据库表设计

以下为系统核心业务表的结构设计。

4.1 车辆表 (tb_vehicle)

存储车辆基本信息、当前状态、物理坐标及乐观锁版本号。

CREATE TABLE `tb_vehicle` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `vehicle_no` varchar(32) NOT NULL COMMENT '车辆唯一编号',
  `status` varchar(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态：AVAILABLE-可租, RESERVED-预占, RENTING-租赁中, CHARGING-充电中, REPAIRING-维修中',
  `battery_level` int NOT NULL DEFAULT 100 COMMENT '剩余电量百分比(0-100)',
  `health_score` int NOT NULL DEFAULT 100 COMMENT '健康度评分(0-100)',
  `latitude` decimal(10, 7) NOT NULL COMMENT '当前纬度',
  `longitude` decimal(10, 7) NOT NULL COMMENT '当前经度',
  `geohash` varchar(12) NOT NULL COMMENT 'GeoHash编码',
  `last_used_time` datetime DEFAULT NULL COMMENT '最后一次使用时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_vehicle_no` (`vehicle_no`),
  KEY `idx_geohash` (`geohash`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆信息表';

4.2 租赁订单表 (tb_order)

记录用户租赁订单的生命周期和计费详情。

CREATE TABLE `tb_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单主键ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `vehicle_id` bigint NOT NULL COMMENT '车辆ID',
  `status` varchar(20) NOT NULL DEFAULT 'CREATED' COMMENT '订单状态：CREATED-已创建, PAID-已支付, CANCELLED-已取消, RUNNING-进行中, FINISHED-已完成',
  `start_time` datetime DEFAULT NULL COMMENT '租车开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '还车结束时间',
  `start_lat` decimal(10, 7) DEFAULT NULL COMMENT '起点纬度',
  `start_lng` decimal(10, 7) DEFAULT NULL COMMENT '起点经度',
  `end_lat` decimal(10, 7) DEFAULT NULL COMMENT '终点纬度',
  `end_lng` decimal(10, 7) DEFAULT NULL COMMENT '终点经度',
  `amount` decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '订单金额',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_vehicle_id` (`vehicle_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁订单表';

4.3 网格运力状态表 (tb_grid_dispatch)

用于维护特定网格内的运力供需指标，提供调度决策数据。

CREATE TABLE `tb_grid_dispatch` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `geohash_prefix` varchar(8) NOT NULL COMMENT '网格GeoHash前缀(如5位或6位)',
  `vehicle_count` int NOT NULL DEFAULT 0 COMMENT '网格内空闲车辆数',
  `demand_count` int NOT NULL DEFAULT 0 COMMENT '近期订单需求数(如近15分钟)',
  `supply_demand_ratio` decimal(5, 2) NOT NULL DEFAULT '1.00' COMMENT '供需比(车辆数/需求数)',
  `price_factor` decimal(3, 2) NOT NULL DEFAULT '1.00' COMMENT '动态定价调价系数(如1.0-1.5)',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_geohash_prefix` (`geohash_prefix`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网格运力与调度表';

5. 核心设计说明

5.1 Redis Lua 预占逻辑

在高并发抢占车辆时，为防止超卖（多个用户抢占同一辆车），系统在 Redis 端执行 Lua 脚本进行原子校验与状态修改。

-- Lua 脚本伪代码示例：check_and_reserve.lua
local vehicle_key = KEYS[1]
local user_id = ARGV[1]
local expire_time = ARGV[2]

-- 获取车辆当前状态
local status = redis.call('HGET', vehicle_key, 'status')

if status == 'AVAILABLE' then
    -- 将状态变更为预占 (RESERVED)
    redis.call('HSET', vehicle_key, 'status', 'RESERVED')
    redis.call('HSET', vehicle_key, 'lock_user', user_id)
    -- 设置预占过期时间，防止死锁
    redis.call('EXPIRE', vehicle_key, expire_time)
    return 1 -- 预占成功
else
    return 0 -- 车辆已被占用或处于非空闲状态
end

5.2 状态机迁移校验

后端借助状态机模式，显式定义状态转移矩阵，拦截异常的状态跃迁请求：

| 源状态 \\ 目标状态   | AVAILABLE | RESERVED | RENTING | CHARGING | REPAIRING |
| :------------ | :-------: | :------: | :-----: | :------: | :-------: |
| **AVAILABLE** | ❌         | (预占)     | ❌       | (充电)     | (报修)      |
| **RESERVED**  | (超时取消)    | ❌        | (开锁用车)  | ❌        | ❌         |
| **RENTING**   | ❌         | ❌        | ❌       | (还车/低电)  | (还车/故障)   |
| **CHARGING**  | (充完电)     | ❌        | ❌       | ❌        | (充电故障)    |
| **REPAIRING** | (修复)      | ❌        | ❌       | ❌        | ❌         |

