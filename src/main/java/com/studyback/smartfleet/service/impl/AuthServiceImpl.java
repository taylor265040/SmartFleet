package com.studyback.smartfleet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyback.smartfleet.dto.LoginDTO;
import com.studyback.smartfleet.dto.UserDTO;
import com.studyback.smartfleet.entity.RoleEnum;
import com.studyback.smartfleet.entity.User;
import com.studyback.smartfleet.exception.BusinessException;
import com.studyback.smartfleet.mapper.UserMapper;
import com.studyback.smartfleet.response.ResultCode;
import com.studyback.smartfleet.service.AuthService;
import com.studyback.smartfleet.service.UserService;
import com.studyback.smartfleet.util.JwtUtil;
import com.studyback.smartfleet.vo.TokenVO;
import com.studyback.smartfleet.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证 Service 实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(UserDTO userDTO) {
        log.info("用户注册: username={}", userDTO.getUsername());

        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, userDTO.getUsername());
        Long count = userMapper.selectCount(wrapper);
        if (count > 0) {
            log.warn("用户名已存在: {}", userDTO.getUsername());
            throw new BusinessException(ResultCode.CONFLICT, "用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setPhone(userDTO.getPhone());
        user.setRole(RoleEnum.ROLE_USER.getCode());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setIsDeleted(0);

        userMapper.insert(user);
        log.info("用户注册成功: id={}, username={}", user.getId(), user.getUsername());

        return userService.toVO(user);
    }

    @Override
    public TokenVO login(LoginDTO loginDTO) {
        log.info("用户登录: username={}", loginDTO.getUsername());

        // 根据用户名查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            log.warn("用户不存在: {}", loginDTO.getUsername());
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            log.warn("密码错误: username={}", loginDTO.getUsername());
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // 生成 Token
        TokenVO tokenVO = generateTokens(user);
        log.info("用户登录成功: id={}, username={}", user.getId(), user.getUsername());

        return tokenVO;
    }

    @Override
    public TokenVO refreshToken(String refreshToken) {
        log.info("刷新 Token");

        // 验证 Refresh Token
        if (!jwtUtil.validateToken(refreshToken)) {
            log.warn("Refresh Token 无效或已过期");
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Refresh Token 无效或已过期");
        }

        // 验证 Token 类型必须为 refresh
        String tokenType = jwtUtil.extractTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            log.warn("Token 类型错误: expected=refresh, actual={}", tokenType);
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Token 类型错误，请使用 Refresh Token");
        }

        // 从 Token 中提取用户名
        String username = jwtUtil.extractUsername(refreshToken);

        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            log.warn("用户不存在: {}", username);
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户不存在");
        }

        // 生成新的 Token
        TokenVO tokenVO = generateTokens(user);
        log.info("Token 刷新成功: username={}", username);

        return tokenVO;
    }

    @Override
    public UserVO getCurrentUser() {
        // 从 Security 上下文获取用户名
        String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        log.info("获取当前用户信息: username={}", username);

        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            log.warn("当前用户不存在: {}", username);
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        return userService.toVO(user);
    }

    /**
     * 生成访问 Token 和刷新 Token
     */
    private TokenVO generateTokens(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), List.of(user.getRole()));
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        return TokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpirationInSeconds())
                .build();
    }
}
