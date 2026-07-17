package com.studyback.smartfleet.service;

import com.studyback.smartfleet.entity.GeoHashPoint;
import java.util.List;

/**
 * GeoHash 服务接口
 * <p>提供 GeoHash 编码、解码、邻居计算、半径内网格查询等功能</p>
 */
public interface GeoHashService {

    /**
     * GeoHash 编码
     *
     * @param lat       纬度 [-90, 90]
     * @param lng       经度 [-180, 180]
     * @param precision 精度（字符长度），范围 [1, 12]
     * @return GeoHash 编码字符串
     */
    String encode(double lat, double lng, int precision);

    /**
     * GeoHash 解码
     *
     * @param geoHash GeoHash 编码字符串
     * @return 解码后的坐标点（纬度, 经度）
     */
    GeoHashPoint decode(String geoHash);

    /**
     * 获取 8 个邻居网格
     *
     * @param geoHash GeoHash 编码字符串
     * @return 8 个邻居 GeoHash 编码列表
     */
    List<String> getNeighbors(String geoHash);

    /**
     * 获取指定半径内的所有网格
     *
     * @param lat    中心纬度
     * @param lng    中心经度
     * @param radius 半径（公里）
     * @return 半径内所有 GeoHash 编码列表
     */
    List<String> getGridsInRadius(double lat, double lng, double radius);
}
