package com.studyback.smartfleet.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token 视图对象
 * <p>用于登录和刷新 Token 接口返回</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenVO {

    /** 访问 Token */
    private String accessToken;

    /** 刷新 Token */
    private String refreshToken;

    /** Token 类型 */
    @Builder.Default
    private String tokenType = "Bearer";

    /** 访问 Token 有效时间（秒） */
    private Long expiresIn;
}
