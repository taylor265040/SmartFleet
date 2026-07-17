# Task 2: M2 - 用户认证与安全

## 模块概述
集成Spring Security + JWT，实现用户注册、登录、Token刷新流程，设计并创建用户表，实现基于角色的接口权限控制。

---

## 子步骤

### 1. 工程准备
- [ ] 添加Spring Security依赖
- [ ] 添加JWT依赖（jjwt或spring-security-oauth2-jose）
- [ ] 配置Spring Security基础设置
- [ ] 创建用户实体类User（id, username, password, phone, role, create_time等）
- [ ] 创建UserMapper接口

### 2. 测试先行
- [ ] 编写用户注册接口测试
- [ ] 编写用户登录接口测试
- [ ] 编写JWT Token生成和验证测试
- [ ] 编写Token刷新接口测试
- [ ] 编写权限控制测试（不同角色访问不同接口）

### 3. 硬编码跑通
- [ ] 实现用户注册接口（硬编码验证逻辑）
- [ ] 实现用户登录接口（硬编码用户名密码验证）
- [ ] 实现JWT Token生成（硬编码密钥和过期时间）
- [ ] 实现JWT Token验证过滤器
- [ ] 验证登录流程可正常工作

### 4. 骨架
- [ ] 创建UserService接口和实现类
- [ ] 创建AuthController（注册、登录、刷新Token接口）
- [ ] 创建SecurityConfig配置类
- [ ] 创建JWT工具类（生成、解析、验证Token）
- [ ] 创建JWT认证过滤器JwtAuthenticationFilter

### 5. 数据加载
- [ ] 实现用户注册逻辑（密码BCrypt加密存储）
- [ ] 实现用户登录逻辑（数据库验证用户名密码）
- [ ] 实现JWT Token生成（从数据库加载用户角色）
- [ ] 实现Token刷新逻辑（验证旧Token，生成新Token）
- [ ] 创建用户角色枚举（ROLE_USER, ROLE_ADMIN等）

### 6. 检索实现
- [ ] 实现用户信息查询接口（根据Token获取当前用户）
- [ ] 实现用户列表查询接口（仅管理员可访问）
- [ ] 实现用户角色查询接口
- [ ] 实现Token有效性验证接口

### 7. 集成具体实现
- [ ] 配置Spring Security过滤器链
- [ ] 实现基于角色的访问控制（@PreAuthorize）
- [ ] 实现接口权限注解（@RequireRole）
- [ ] 配置CORS跨域
- [ ] 配置公开接口白名单（/api/auth/login, /api/auth/register）
- [ ] 编写集成测试验证完整认证流程

---

## 测试用例记录 (test-record)

### 单元测试用例

#### 1. 正常测试用例
```java
@Test
void testUserRegistration() {
    // 测试用户注册
    UserDTO userDTO = new UserDTO("testuser", "password123", "13800138000");
    User registeredUser = userService.register(userDTO);
    
    assertNotNull(registeredUser.getId());
    assertEquals("testuser", registeredUser.getUsername());
    assertNotEquals("password123", registeredUser.getPassword()); // 密码应已加密
}

@Test
void testUserLogin() {
    // 测试用户登录
    LoginDTO loginDTO = new LoginDTO("testuser", "password123");
    String token = authService.login(loginDTO);
    
    assertNotNull(token);
    assertTrue(token.length() > 0);
}

@Test
void testJWTTokenGeneration() {
    // 测试JWT Token生成
    User user = new User(1L, "testuser", "ROLE_USER");
    String token = jwtUtil.generateToken(user);
    
    assertNotNull(token);
    assertEquals("testuser", jwtUtil.extractUsername(token));
}

@Test
void testJWTTokenValidation() {
    // 测试JWT Token验证
    User user = new User(1L, "testuser", "ROLE_USER");
    String token = jwtUtil.generateToken(user);
    
    assertTrue(jwtUtil.validateToken(token, user));
}

@Test
void testTokenRefresh() {
    // 测试Token刷新
    String oldToken = jwtUtil.generateToken(user);
    String newToken = authService.refreshToken(oldToken);
    
    assertNotNull(newToken);
    assertNotEquals(oldToken, newToken);
    assertEquals(user.getUsername(), jwtUtil.extractUsername(newToken));
}
```

#### 2. 边界测试用例
```java
@Test
void testRegistrationWithDuplicateUsername() {
    // 测试重复用户名注册
    UserDTO userDTO = new UserDTO("existinguser", "password123", "13800138000");
    userService.register(userDTO);
    
    assertThrows(BusinessException.class, () -> {
        userService.register(userDTO);
    });
}

@Test
void testLoginWithWrongPassword() {
    // 测试错误密码登录
    LoginDTO loginDTO = new LoginDTO("testuser", "wrongpassword");
    
    assertThrows(BusinessException.class, () -> {
        authService.login(loginDTO);
    });
}

@Test
void testLoginWithNonExistentUser() {
    // 测试不存在的用户登录
    LoginDTO loginDTO = new LoginDTO("nonexistent", "password123");
    
    assertThrows(BusinessException.class, () -> {
        authService.login(loginDTO);
    });
}

@Test
void testExpiredToken() {
    // 测试过期Token
    String token = jwtUtil.generateToken(user);
    // 模拟Token过期
    Thread.sleep(tokenExpiration + 1000);
    
    assertFalse(jwtUtil.validateToken(token, user));
}

@Test
void testInvalidToken() {
    // 测试无效Token
    String invalidToken = "invalid.token.here";
    
    assertThrows(JwtException.class, () -> {
        jwtUtil.extractUsername(invalidToken);
    });
}
```

#### 3. 失败测试用例
```java
@Test
void testRegistrationWithInvalidPhone() {
    // 测试无效手机号注册
    UserDTO userDTO = new UserDTO("testuser", "password123", "invalidphone");
    
    assertThrows(BusinessException.class, () -> {
        userService.register(userDTO);
    });
}

@Test
void testRegistrationWithWeakPassword() {
    // 测试弱密码注册
    UserDTO userDTO = new UserDTO("testuser", "123", "13800138000");
    
    assertThrows(BusinessException.class, () -> {
        userService.register(userDTO);
    });
}

@Test
void testTokenRefreshWithExpiredToken() {
    // 测试使用过期Token刷新
    String expiredToken = jwtUtil.generateToken(user);
    // 模拟Token过期
    Thread.sleep(tokenExpiration + 1000);
    
    assertThrows(BusinessException.class, () -> {
        authService.refreshToken(expiredToken);
    });
}
```

### 集成测试用例

#### 1. 正常流程测试
```java
@Test
void testFullAuthenticationFlow() {
    // 测试完整认证流程
    // 1. 用户注册
    UserDTO userDTO = new UserDTO("testuser", "password123", "13800138000");
    User registeredUser = userService.register(userDTO);
    assertNotNull(registeredUser);
    
    // 2. 用户登录
    LoginDTO loginDTO = new LoginDTO("testuser", "password123");
    String token = authService.login(loginDTO);
    assertNotNull(token);
    
    // 3. 使用Token访问受保护接口
    mockMvc.perform(get("/api/users/me")
            .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("testuser"));
    
    // 4. 刷新Token
    String newToken = authService.refreshToken(token);
    assertNotNull(newToken);
    
    // 5. 使用新Token访问接口
    mockMvc.perform(get("/api/users/me")
            .header("Authorization", "Bearer " + newToken))
            .andExpect(status().isOk());
}
```

#### 2. 权限控制测试
```java
@Test
void testRoleBasedAccessControl() {
    // 测试基于角色的访问控制
    // 1. 创建普通用户
    UserDTO userDTO = new UserDTO("normaluser", "password123", "13800138000");
    User normalUser = userService.register(userDTO);
    String userToken = authService.login(new LoginDTO("normaluser", "password123"));
    
    // 2. 创建管理员用户
    UserDTO adminDTO = new UserDTO("adminuser", "password123", "13800138001");
    User adminUser = userService.register(adminDTO);
    // 设置管理员角色
    adminUser.setRole("ROLE_ADMIN");
    userService.updateUser(adminUser);
    String adminToken = authService.login(new LoginDTO("adminuser", "password123"));
    
    // 3. 普通用户访问管理员接口 - 应返回403
    mockMvc.perform(get("/api/admin/users")
            .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isForbidden());
    
    // 4. 管理员访问管理员接口 - 应返回200
    mockMvc.perform(get("/api/admin/users")
            .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
}
```

#### 3. 异常场景测试
```java
@Test
void testUnauthorizedAccess() {
    // 测试未授权访问
    mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized());
}

@Test
void testAccessWithExpiredToken() {
    // 测试使用过期Token访问
    String token = jwtUtil.generateToken(user);
    // 模拟Token过期
    Thread.sleep(tokenExpiration + 1000);
    
    mockMvc.perform(get("/api/users/me")
            .header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized());
}

@Test
void testAccessWithInvalidToken() {
    // 测试使用无效Token访问
    mockMvc.perform(get("/api/users/me")
            .header("Authorization", "Bearer invalid.token.here"))
            .andExpect(status().isUnauthorized());
}
```

---

## 验收标准
- [ ] 用户注册功能正常，密码加密存储
- [ ] 用户登录功能正常，返回有效JWT Token
- [ ] JWT Token生成、解析、验证功能正常
- [ ] Token刷新功能正常
- [ ] 基于角色的访问控制生效
- [ ] 公开接口（登录、注册）可正常访问
- [ ] 受保护接口需要有效Token才能访问
- [ ] 单元测试覆盖率 ≥ 80%
- [ ] 集成测试通过

## 依赖项
- Spring Security 6.x
- JWT库（jjwt 0.11.x或spring-security-oauth2-jose）
- BCrypt密码加密

## 风险与注意事项
1. JWT密钥需要安全存储（建议使用环境变量或配置中心）
2. Token过期时间需要合理设置（建议访问Token 30分钟，刷新Token 7天）
3. 密码加密强度需要足够（BCrypt rounds建议10-12）
4. 需要处理Token刷新时的并发问题
5. 需要记录登录日志和安全审计日志