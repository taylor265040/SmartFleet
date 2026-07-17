package com.studyback.smartfleet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新 Token DTO
 */
@Data
public class RefreshTokenDTO {

    /** 刷新 Token */
    @NotBlank(message = "refreshToken不能为空")
    private String refreshToken;
}
