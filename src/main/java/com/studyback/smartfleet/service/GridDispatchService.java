package com.studyback.smartfleet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.studyback.smartfleet.entity.GridDispatch;
import java.util.Map;

/**
 * 网格运力 Service 接口
 */
public interface GridDispatchService extends IService<GridDispatch> {

    /**
     * 计算供需比（可用车辆数 / 活跃订单数）
     *
     * @param gridId 网格 GeoHash 编码
     * @return 供需比，无订单时返回无穷大
     */
    double calculateSupplyDemandRatio(String gridId);

    /**
     * 更新网格运力数据（根据车辆和订单实时统计）
     *
     * @param gridId 网格 GeoHash 编码
     */
    void updateGridCapacity(String gridId);

    /**
     * 判断是否低供区域（供需比 < 0.5）
     *
     * @param gridId 网格 GeoHash 编码
     * @return true 表示低供区域
     */
    boolean isLowSupplyArea(String gridId);

    /**
     * 获取车辆分布统计
     *
     * @return key 为 GeoHash，value 为该网格的车辆数
     */
    Map<String, Integer> getVehicleDistribution();
}
