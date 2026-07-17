package com.studyback.smartfleet.service;

import com.studyback.smartfleet.entity.DispatchSuggestion;
import java.util.List;

/**
 * 运力调度服务接口
 * <p>根据网格供需情况生成运力调度建议</p>
 */
public interface CapacityDispatchService {

    /**
     * 生成单个网格的调度建议
     *
     * @param gridId 网格 GeoHash 编码
     * @return 调度建议，高供区域返回 null
     */
    DispatchSuggestion generateSuggestion(String gridId);

    /**
     * 批量生成调度建议
     *
     * @param gridIds 网格 GeoHash 编码列表
     * @return 调度建议列表（仅包含低供区域的建议）
     */
    List<DispatchSuggestion> generateSuggestionsForArea(List<String> gridIds);
}
