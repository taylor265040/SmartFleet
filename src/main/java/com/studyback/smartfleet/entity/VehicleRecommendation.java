package com.studyback.smartfleet.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 车辆推荐响应 VO
 * <p>封装推荐给用户的车辆信息和评分</p>
 */
@Data
public class VehicleRecommendation implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 车辆ID */
    private Long vehicleId;

    /** 车牌号 */
    private String plateNo;

    /** 车型 */
    private String model;

    /** 当前纬度（地图推荐时有值，列表推荐时为null） */
    private BigDecimal latitude;

    /** 当前经度（地图推荐时有值，列表推荐时为null） */
    private BigDecimal longitude;

    /** 电量百分比 */
    private Integer batteryLevel;

    /** 健康度评分 */
    private Integer healthScore;

    /** 车辆状态 */
    private String status;

    /** 综合评分（0-100） */
    private Double score;

    /** 距离（公里） */
    private Double distance;

    public VehicleRecommendation() {
    }

    /**
     * 从车辆实体构建推荐对象（含坐标，用于地图推荐）
     */
    public static VehicleRecommendation fromVehicleWithLocation(Vehicle vehicle, double score, double distance) {
        VehicleRecommendation rec = new VehicleRecommendation();
        rec.setVehicleId(vehicle.getId());
        rec.setPlateNo(vehicle.getPlateNo());
        rec.setModel(vehicle.getModel());
        rec.setLatitude(vehicle.getCurrentLat());
        rec.setLongitude(vehicle.getCurrentLng());
        rec.setBatteryLevel(vehicle.getBatteryLevel());
        rec.setHealthScore(vehicle.getHealthScore());
        rec.setStatus(vehicle.getStatus());
        rec.setScore(score);
        rec.setDistance(distance);
        return rec;
    }

    /**
     * 从车辆实体构建推荐对象（不含坐标，用于列表推荐）
     */
    public static VehicleRecommendation fromVehicleWithoutLocation(Vehicle vehicle, double score, double distance) {
        VehicleRecommendation rec = new VehicleRecommendation();
        rec.setVehicleId(vehicle.getId());
        rec.setPlateNo(vehicle.getPlateNo());
        rec.setModel(vehicle.getModel());
        rec.setBatteryLevel(vehicle.getBatteryLevel());
        rec.setHealthScore(vehicle.getHealthScore());
        rec.setStatus(vehicle.getStatus());
        rec.setScore(score);
        rec.setDistance(distance);
        return rec;
    }
}
