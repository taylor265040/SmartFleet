package com.studyback.smartfleet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyback.smartfleet.entity.StateChangeRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 车辆状态变更记录 Mapper 接口
 */
@Mapper
public interface StateChangeRecordMapper extends BaseMapper<StateChangeRecord> {
}
