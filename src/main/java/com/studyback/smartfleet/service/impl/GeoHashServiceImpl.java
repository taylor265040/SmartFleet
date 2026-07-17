package com.studyback.smartfleet.service.impl;

import com.studyback.smartfleet.entity.GeoHashPoint;
import com.studyback.smartfleet.exception.BusinessException;
import com.studyback.smartfleet.response.ResultCode;
import com.studyback.smartfleet.service.GeoHashService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * GeoHash 服务实现类
 * <p>使用 Base32 编码实现 GeoHash 算法</p>
 * <p>字符集：0123456789bcdefghjkmnpqrstuvwxyz（32 个字符）</p>
 */
@Slf4j
@Service
public class GeoHashServiceImpl implements GeoHashService {

    /** GeoHash Base32 字符集 */
    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";

    /** 纬度范围 */
    private static final double LAT_MIN = -90.0;
    private static final double LAT_MAX = 90.0;

    /** 经度范围 */
    private static final double LNG_MIN = -180.0;
    private static final double LNG_MAX = 180.0;

    /** 默认精度 */
    private static final int DEFAULT_PRECISION = 6;

    /** 最大精度 */
    private static final int MAX_PRECISION = 12;

    @Override
    public String encode(double lat, double lng, int precision) {
        validateCoordinates(lat, lng);
        validatePrecision(precision);

        boolean isEven = true;
        int bit = 0;
        int ch = 0;
        double[] latRange = {LAT_MIN, LAT_MAX};
        double[] lngRange = {LNG_MIN, LNG_MAX};

        StringBuilder geoHash = new StringBuilder();

        while (geoHash.length() < precision) {
            if (isEven) {
                // 经度二分
                double mid = (lngRange[0] + lngRange[1]) / 2;
                if (lng >= mid) {
                    ch |= (1 << (4 - bit));
                    lngRange[0] = mid;
                } else {
                    lngRange[1] = mid;
                }
            } else {
                // 纬度二分
                double mid = (latRange[0] + latRange[1]) / 2;
                if (lat >= mid) {
                    ch |= (1 << (4 - bit));
                    latRange[0] = mid;
                } else {
                    latRange[1] = mid;
                }
            }
            isEven = !isEven;

            if (bit < 4) {
                bit++;
            } else {
                geoHash.append(BASE32.charAt(ch));
                bit = 0;
                ch = 0;
            }
        }

        log.info("GeoHash编码: lat={}, lng={}, precision={}, result={}", lat, lng, precision, geoHash);
        return geoHash.toString();
    }

    @Override
    public GeoHashPoint decode(String geoHash) {
        validateGeoHash(geoHash);

        boolean isEven = true;
        double[] latRange = {LAT_MIN, LAT_MAX};
        double[] lngRange = {LNG_MIN, LNG_MAX};

        for (char c : geoHash.toCharArray()) {
            int idx = BASE32.indexOf(c);
            if (idx < 0) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "GeoHash包含无效字符: " + c);
            }

            for (int i = 4; i >= 0; i--) {
                int bit = (idx >> i) & 1;
                if (isEven) {
                    // 经度
                    double mid = (lngRange[0] + lngRange[1]) / 2;
                    if (bit == 1) {
                        lngRange[0] = mid;
                    } else {
                        lngRange[1] = mid;
                    }
                } else {
                    // 纬度
                    double mid = (latRange[0] + latRange[1]) / 2;
                    if (bit == 1) {
                        latRange[0] = mid;
                    } else {
                        latRange[1] = mid;
                    }
                }
                isEven = !isEven;
            }
        }

        double lat = (latRange[0] + latRange[1]) / 2;
        double lng = (lngRange[0] + lngRange[1]) / 2;

        log.info("GeoHash解码: geoHash={}, lat={}, lng={}", geoHash, lat, lng);
        return new GeoHashPoint(lat, lng);
    }

    @Override
    public List<String> getNeighbors(String geoHash) {
        validateGeoHash(geoHash);

        GeoHashPoint center = decode(geoHash);
        int precision = geoHash.length();

        // 通过中心点的边界计算邻居
        // GeoHash 的精度对应每个维度的步长
        double[] latStep = getLatStep(precision);
        double[] lngStep = getLngStep(precision);

        double latErr = (latStep[1] - latStep[0]) / 2;
        double lngErr = (lngStep[1] - lngStep[0]) / 2;

        Set<String> neighbors = new LinkedHashSet<>();

        // 8 个方向的偏移
        double[][] offsets = {
                {-latErr, -lngErr}, {-latErr, 0}, {-latErr, lngErr},
                {0, -lngErr}, {0, lngErr},
                {latErr, -lngErr}, {latErr, 0}, {latErr, lngErr}
        };

        for (double[] offset : offsets) {
            double nLat = center.getLatitude() + offset[0];
            double nLng = center.getLongitude() + offset[1];
            // 处理边界情况
            if (nLat > LAT_MAX) nLat = LAT_MAX;
            if (nLat < LAT_MIN) nLat = LAT_MIN;
            if (nLng > LNG_MAX) nLng = LNG_MAX;
            if (nLng < LNG_MIN) nLng = LNG_MIN;
            neighbors.add(encode(nLat, nLng, precision));
        }

        // 确保返回恰好 8 个（排除自身）
        neighbors.remove(geoHash);

        log.info("GeoHash邻居: geoHash={}, neighbors={}", geoHash, neighbors);
        return new ArrayList<>(neighbors);
    }

    @Override
    public List<String> getGridsInRadius(double lat, double lng, double radius) {
        validateCoordinates(lat, lng);

        if (radius <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "半径必须大于0");
        }

        // 根据半径估算需要的精度
        int precision = estimatePrecision(radius);

        // 中心网格
        String centerHash = encode(lat, lng, precision);

        // 收集所有候选网格
        Set<String> grids = new LinkedHashSet<>();
        grids.add(centerHash);

        // 计算半径对应的经纬度偏移
        // 1度纬度约111km，1度经度约111*cos(lat)km
        double latDelta = radius / 111.0;
        double lngDelta = radius / (111.0 * Math.cos(Math.toRadians(lat)));

        // 在矩形范围内采样网格
        double step = getStepForPrecision(precision);
        for (double dLat = -latDelta; dLat <= latDelta; dLat += step) {
            for (double dLng = -lngDelta; dLng <= lngDelta; dLng += step) {
                double checkLat = lat + dLat;
                double checkLng = lng + dLng;
                if (checkLat >= LAT_MIN && checkLat <= LAT_MAX
                        && checkLng >= LNG_MIN && checkLng <= LNG_MAX) {
                    grids.add(encode(checkLat, checkLng, precision));
                }
            }
        }

        log.info("半径内网格: lat={}, lng={}, radius={}km, precision={}, gridCount={}",
                lat, lng, radius, precision, grids.size());
        return new ArrayList<>(grids);
    }

    /**
     * 校验坐标合法性
     */
    private void validateCoordinates(double lat, double lng) {
        if (Double.isNaN(lat) || Double.isNaN(lng)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "坐标不能为NaN");
        }
        if (Double.isInfinite(lat) || Double.isInfinite(lng)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "坐标不能为无穷大");
        }
        if (lat < LAT_MIN || lat > LAT_MAX) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "纬度超出范围[-90, 90]: " + lat);
        }
        if (lng < LNG_MIN || lng > LNG_MAX) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "经度超出范围[-180, 180]: " + lng);
        }
    }

    /**
     * 校验精度合法性
     */
    private void validatePrecision(int precision) {
        if (precision < 1 || precision > MAX_PRECISION) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "精度必须在[1, 12]范围内: " + precision);
        }
    }

    /**
     * 校验 GeoHash 字符串
     */
    private void validateGeoHash(String geoHash) {
        if (geoHash == null || geoHash.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "GeoHash不能为空");
        }
        for (char c : geoHash.toCharArray()) {
            if (BASE32.indexOf(c) < 0) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "GeoHash包含无效字符: " + c);
            }
        }
    }

    /**
     * 根据半径估算合适的精度
     * <p>精度6约1.2km x 0.6km，精度5约5km x 5km</p>
     */
    private int estimatePrecision(double radiusKm) {
        if (radiusKm <= 1.0) return 7;
        if (radiusKm <= 2.5) return 6;
        if (radiusKm <= 10.0) return 5;
        if (radiusKm <= 40.0) return 4;
        if (radiusKm <= 150.0) return 3;
        return 2;
    }

    /**
     * 获取指定精度对应的纬度步长范围
     */
    private double[] getLatStep(int precision) {
        double range = LAT_MAX - LAT_MIN;
        double step = range / Math.pow(2, precision * 5.0 / 2);
        return new double[]{-step / 2, step / 2};
    }

    /**
     * 获取指定精度对应的经度步长范围
     */
    private double[] getLngStep(int precision) {
        double range = LNG_MAX - LNG_MIN;
        double step = range / Math.pow(2, precision * 5.0 / 2);
        return new double[]{-step / 2, step / 2};
    }

    /**
     * 获取指定精度对应的网格边长（度）
     */
    private double getStepForPrecision(int precision) {
        // 每个精度大约对应的度数
        return 360.0 / Math.pow(2, precision * 5.0 / 2);
    }
}
