package com.studyback.smartfleet.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图对象
 * <p>用于 API 返回，不包含 password 等敏感字段</p>
 */
@Data
public class UserVO {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 手机号 */
    private String phone;

    /** 角色：ADMIN/USER */
    private String role;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
