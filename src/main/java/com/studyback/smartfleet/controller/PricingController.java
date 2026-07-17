package com.studyback.smartfleet.controller;

import com.studyback.smartfleet.entity.DispatchSuggestion;
import com.studyback.smartfleet.response.ApiResponse;
import com.studyback.smartfleet.service.CapacityDispatchService;
import com.studyback.smartfleet.service.DynamicPricingService;
import com.studyback.smartfleet.service.GridDispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 定价 Controller
 */
@Tag(name = "动态定价管理", description = "动态定价和调度建议相关接口")
@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final DynamicPricingService dynamicPricingService;
    private final CapacityDispatchService capacityDispatchService;
    private final GridDispatchService gridDispatchService;

    /**
     * 查询动态价格
     */
    @Operation(summary = "查询动态价格")
    @GetMapping("/{geohash}")
    public ApiResponse<Map<String, Object>> getDynamicPrice(
            @Parameter(description = "GeoHash编码") @PathVariable String geohash,
            @Parameter(description = "基础价格") @RequestParam(defaultValue = "10.0") double basePrice) {
        double price = dynamicPricingService.calculatePrice(geohash, LocalDateTime.now(), basePrice);

        Map<String, Object> result = Map.of(
                "geohash", geohash,
                "basePrice", basePrice,
                "dynamicPrice", price,
                "timestamp", LocalDateTime.now().toString()
        );
        return ApiResponse.success(result);
    }

    /**
     * 查询调度建议
     */
    @Operation(summary = "查询调度建议")
    @GetMapping("/suggestions")
    public ApiResponse<List<DispatchSuggestion>> getSuggestions(
            @Parameter(description = "GeoHash编码列表（逗号分隔）") @RequestParam(required = false) String geohashes) {

        List<DispatchSuggestion> suggestions;

        if (geohashes != null && !geohashes.isEmpty()) {
            // 指定网格查询
            List<String> gridIds = List.of(geohashes.split(","));
            suggestions = capacityDispatchService.generateSuggestionsForArea(gridIds);
        } else {
            // 查询所有低供区域的调度建议
            // 获取所有网格，筛选低供区域生成建议
            List<String> allGridIds = gridDispatchService.list().stream()
                    .map(com.studyback.smartfleet.entity.GridDispatch::getGeohash)
                    .toList();
            suggestions = capacityDispatchService.generateSuggestionsForArea(allGridIds);
        }

        return ApiResponse.success(suggestions);
    }
}
