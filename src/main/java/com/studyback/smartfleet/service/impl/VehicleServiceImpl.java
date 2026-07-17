package com.studyback.smartfleet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyback.smartfleet.entity.Vehicle;
import com.studyback.smartfleet.mapper.VehicleMapper;
import com.studyback.smartfleet.service.VehicleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 车辆 Service 实现类
 */
@Slf4j
@Service
public class VehicleServiceImpl extends ServiceImpl<VehicleMapper, Vehicle> implements VehicleService {
}
