package com.studyback.smartfleet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.studyback.smartfleet.entity.User;
import com.studyback.smartfleet.vo.UserVO;

import java.util.List;

/**
 * 用户 Service 接口
 */
public interface UserService extends IService<User> {

    /**
     * 将 User 实体转换为 UserVO（去除敏感字段）
     *
     * @param user 用户实体
     * @return 用户视图对象
     */
    UserVO toVO(User user);

    /**
     * 将 User 实体列表转换为 UserVO 列表
     *
     * @param users 用户实体列表
     * @return 用户视图对象列表
     */
    List<UserVO> toVOList(List<User> users);
}
