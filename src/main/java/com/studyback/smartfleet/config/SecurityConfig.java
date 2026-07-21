package com.studyback.smartfleet.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyback.smartfleet.filter.JwtAuthenticationFilter;
import com.studyback.smartfleet.response.ApiResponse;
import com.studyback.smartfleet.response.ResultCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 配置类
 * <p>配置安全过滤器链、CORS、公开接口白名单等</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 公开接口白名单
     */
    private static final String[] PUBLIC_URLS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            // Swagger/OpenAPI
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            // WebSocket
            "/ws/**"
    };

    /**
     * 密码编码器（BCrypt）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 安全过滤器链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF（REST API 不需要）
                .csrf(AbstractHttpConfigurer::disable)
                // 配置 CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 无状态 Session（使用 JWT）
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置请求授权
                .authorizeHttpRequests(auth -> auth
                        // 公开接口
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        // OPTIONS 请求放行（CORS 预检）
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 管理员接口
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                        // 其他接口需要认证
                        .anyRequest().authenticated()
                )
                // 配置异常处理
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            ApiResponse<Void> apiResponse = ApiResponse.fail(ResultCode.UNAUTHORIZED);
                            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            ApiResponse<Void> apiResponse = ApiResponse.fail(ResultCode.FORBIDDEN);
                            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                        })
                )
                // 添加 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
