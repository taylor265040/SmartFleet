package com.studyback.smartfleet.service;

import com.studyback.smartfleet.dto.LoginDTO;
import com.studyback.smartfleet.dto.UserDTO;
import com.studyback.smartfleet.vo.TokenVO;
import com.studyback.smartfleet.vo.UserVO;

/**
 * 认证 Service 接口
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * @param userDTO 用户注册信息
     * @return 注册成功的用户信息
     */
    UserVO register(UserDTO userDTO);

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return Token 信息
     */
    TokenVO login(LoginDTO loginDTO);

    /**
     * 刷新 Token
     *
     * @param refreshToken 刷新 Token
     * @return 新的 Token 信息
     */
    TokenVO refreshToken(String refreshToken);

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    UserVO getCurrentUser();
}
