package com.studyback.smartfleet.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GeoHash 解码坐标点
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoHashPoint {

    /** 纬度 */
    private Double latitude;

    /** 经度 */
    private Double longitude;
}
