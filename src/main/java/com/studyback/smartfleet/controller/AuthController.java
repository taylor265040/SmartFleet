package com.studyback.smartfleet.controller;

import com.studyback.smartfleet.dto.LoginDTO;
import com.studyback.smartfleet.dto.RefreshTokenDTO;
import com.studyback.smartfleet.dto.UserDTO;
import com.studyback.smartfleet.response.ApiResponse;
import com.studyback.smartfleet.service.AuthService;
import com.studyback.smartfleet.vo.TokenVO;
import com.studyback.smartfleet.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证 Controller
 * <p>提供用户注册、登录、Token 刷新、获取当前用户信息等接口</p>
 */
@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ApiResponse<UserVO> register(@Valid @RequestBody UserDTO userDTO) {
        UserVO userVO = authService.register(userDTO);
        return ApiResponse.success("注册成功", userVO);
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<TokenVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        TokenVO tokenVO = authService.login(loginDTO);
        return ApiResponse.success("登录成功", tokenVO);
    }

    /**
     * 刷新 Token
     */
    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public ApiResponse<TokenVO> refreshToken(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO) {
        TokenVO tokenVO = authService.refreshToken(refreshTokenDTO.getRefreshToken());
        return ApiResponse.success("Token刷新成功", tokenVO);
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public ApiResponse<UserVO> me() {
        UserVO userVO = authService.getCurrentUser();
        return ApiResponse.success(userVO);
    }
}
