package com.studyback.smartfleet.controller;

import com.studyback.smartfleet.entity.User;
import com.studyback.smartfleet.response.ApiResponse;
import com.studyback.smartfleet.service.UserService;
import com.studyback.smartfleet.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户 Controller
 */
@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 根据ID查询用户
     */
    @Operation(summary = "根据ID查询用户")
    @GetMapping("/{id}")
    public ApiResponse<UserVO> getById(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        User user = userService.getById(id);
        return ApiResponse.success(userService.toVO(user));
    }

    /**
     * 查询用户列表
     */
    @Operation(summary = "查询用户列表")
    @GetMapping
    public ApiResponse<List<UserVO>> list() {
        List<User> users = userService.list();
        return ApiResponse.success(userService.toVOList(users));
    }
}
