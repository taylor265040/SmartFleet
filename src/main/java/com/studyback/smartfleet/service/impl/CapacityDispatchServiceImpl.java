package com.studyback.smartfleet.service.impl;

import com.studyback.smartfleet.entity.DispatchSuggestion;
import com.studyback.smartfleet.entity.GridDispatch;
import com.studyback.smartfleet.mapper.DispatchSuggestionMapper;
import com.studyback.smartfleet.service.CapacityDispatchService;
import com.studyback.smartfleet.service.GridDispatchService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 运力调度服务实现类
 * <p>根据网格供需情况生成运力调度建议</p>
 * <p>低供区域（供需比 < 0.5）会生成调度建议，高供区域不生成</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CapacityDispatchServiceImpl implements CapacityDispatchService {

    private final GridDispatchService gridDispatchService;
    private final DispatchSuggestionMapper dispatchSuggestionMapper;

    /** 低供区域阈值 */
    private static final double LOW_SUPPLY_THRESHOLD = 0.5;

    /** 高供区域阈值（供需比 > 2.0 不生成建议） */
    private static final double HIGH_SUPPLY_THRESHOLD = 2.0;

    @Override
    public DispatchSuggestion generateSuggestion(String gridId) {
        if (gridId == null || gridId.isEmpty()) {
            log.warn("生成调度建议失败: gridId为空");
            return null;
        }

        log.info("生成调度建议: gridId={}", gridId);

        // 查询网格运力数据
        GridDispatch dispatch = gridDispatchService.lambdaQuery()
                .eq(GridDispatch::getGeohash, gridId)
                .one();

        if (dispatch == null) {
            log.info("网格运力数据不存在，无法生成建议: gridId={}", gridId);
            return null;
        }

        int availableCount = dispatch.getAvailableCount() != null ? dispatch.getAvailableCount() : 0;
        int demandCount = dispatch.getDemandCount() != null ? dispatch.getDemandCount() : 0;

        // 计算供需比
        double ratio;
        if (demandCount == 0) {
            ratio = availableCount > 0 ? Double.POSITIVE_INFINITY : 0.0;
        } else {
            ratio = (double) availableCount / demandCount;
        }

        // 高供区域不生成调度建议
        if (ratio > HIGH_SUPPLY_THRESHOLD) {
            log.info("高供区域，不生成调度建议: gridId={}, ratio={}", gridId, ratio);
            return null;
        }

        // 非低供区域也不生成建议
        if (ratio >= LOW_SUPPLY_THRESHOLD) {
            log.info("供需平衡区域，不生成调度建议: gridId={}, ratio={}", gridId, ratio);
            return null;
        }

        // 生成低供区域的调度建议
        // 建议调度车辆数 = 需求量 * 0.5 - 可用量（至少调度1辆）
        int suggestedCount = Math.max(1, (int) Math.ceil(demandCount * 0.5 - availableCount));

        DispatchSuggestion suggestion = new DispatchSuggestion();
        suggestion.setTargetGridId(gridId);
        suggestion.setCurrentVehicleCount(availableCount);
        suggestion.setSuggestedVehicleCount(suggestedCount);
        suggestion.setSupplyDemandRatio(BigDecimal.valueOf(ratio));
        suggestion.setPriority(determinePriority(ratio));
        suggestion.setReason(buildReason(gridId, availableCount, demandCount, ratio));
        suggestion.setCreateTime(LocalDateTime.now());

        // 持久化调度建议
        dispatchSuggestionMapper.insert(suggestion);

        log.info("调度建议生成完成: gridId={}, suggestedCount={}, priority={}, ratio={}",
                gridId, suggestedCount, suggestion.getPriority(), ratio);

        return suggestion;
    }

    @Override
    public List<DispatchSuggestion> generateSuggestionsForArea(List<String> gridIds) {
        if (gridIds == null || gridIds.isEmpty()) {
            return new ArrayList<>();
        }

        log.info("批量生成调度建议: gridCount={}", gridIds.size());

        List<DispatchSuggestion> suggestions = new ArrayList<>();

        for (String gridId : gridIds) {
            DispatchSuggestion suggestion = generateSuggestion(gridId);
            if (suggestion != null) {
                suggestions.add(suggestion);
            }
        }

        log.info("批量调度建议生成完成: total={}, suggested={}", gridIds.size(), suggestions.size());
        return suggestions;
    }

    /**
     * 根据供需比确定调度优先级
     * <p>供需比 < 0.2: HIGH</p>
     * <p>供需比 0.2-0.4: MEDIUM</p>
     * <p>供需比 0.4-0.5: LOW</p>
     */
    private String determinePriority(double ratio) {
        if (ratio < 0.2) {
            return "HIGH";
        } else if (ratio < 0.4) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * 构建调度原因说明
     */
    private String buildReason(String gridId, int available, int demand, double ratio) {
        return String.format("网格[%s]供需失衡，可用车辆%d辆，活跃订单%d个，供需比%.2f，建议调度车辆补充运力",
                gridId, available, demand, ratio);
    }
}
