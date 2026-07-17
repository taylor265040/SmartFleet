package com.studyback.smartfleet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyback.smartfleet.entity.Vehicle;
import org.apache.ibatis.annotations.Mapper;

/**
 * 车辆 Mapper 接口
 */
@Mapper
public interface VehicleMapper extends BaseMapper<Vehicle> {
}
