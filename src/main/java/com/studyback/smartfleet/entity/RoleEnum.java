package com.studyback.smartfleet.entity;

import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum RoleEnum {

    /** 普通用户 */
    ROLE_USER("ROLE_USER", "普通用户"),

    /** 管理员 */
    ROLE_ADMIN("ROLE_ADMIN", "管理员");

    /** 角色编码 */
    private final String code;

    /** 角色描述 */
    private final String description;

    RoleEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码查找角色
     *
     * @param code 角色编码
     * @return 角色枚举
     * @throws IllegalArgumentException 如果编码无效
     */
    public static RoleEnum fromCode(String code) {
        for (RoleEnum role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("无效的角色编码: " + code);
    }
}
