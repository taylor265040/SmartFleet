package com.studyback.smartfleet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyback.smartfleet.entity.User;
import com.studyback.smartfleet.mapper.UserMapper;
import com.studyback.smartfleet.service.UserService;
import com.studyback.smartfleet.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户 Service 实现类
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public UserVO toVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }

    @Override
    public List<UserVO> toVOList(List<User> users) {
        if (users == null) {
            return List.of();
        }
        return users.stream().map(this::toVO).collect(Collectors.toList());
    }
}
