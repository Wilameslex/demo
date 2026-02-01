package com.example.demo.config;

import com.example.demo.filter.JwtAuthFilter;
import com.example.demo.filter.JwtLoginFilter;
import com.example.demo.service.AdminUserService;
import com.example.demo.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private AdminUserService adminUserService;
    @Autowired
    private JwtUtils jwtUtils;

    // 密码加密器（不变）
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 暴露AuthenticationManager供过滤器使用（不变）
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    // 自定义UserDetailsService（供JwtAuthFilter验证admin身份，不变）
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            com.example.demo.entity.AdminUser admin = adminUserService.getByUsername(username);
            if (admin == null || admin.getStatus() == 0) {
                throw new UsernameNotFoundException("账号不存在或已禁用");
            }
            // 角色前缀为"ROLE_"，Spring Security默认要求（不变）
            return User.withUsername(admin.getUsername())
                    .password(admin.getPassword())
                    .roles("ADMIN") // 对应hasRole("ADMIN")，自动加ROLE_前缀
                    .build();
        };
    }

    // 核心修改：配置接口权限（放行用户接口，拦截admin接口）
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // 前后端分离关闭CSRF（不变）
                .authorizeRequests()
                // 1. 【关键新增】放行所有用户端匿名接口（基因搜索、通路搜索等）
                .antMatchers(
                        "/genes/**",          // 基因搜索
                        "/pathway/**",        // 通路搜索
                        "/expression/**",     // 表达搜索
                        "/gene-network/**",   // 基因网络
                        "/variant/**",        // 基因变异
                        "/tools/**",          // 工具（序列提取）
                        "/mydata/**",         // Jbrowse2静态资源
                        "/gene-data/**",       // 基因结构数据（GeneDataController）
                        "/enrichment/**",
                        "tools/gene-search/**",
                        "/gene/**",
                        "/genome/mitochondrion/**",
                        "/genome/overview/**",
                        "/phenotype/**",
                        "/phenotype/total/**"
                ).permitAll() // 匿名用户可访问
                // 2. 【保留原有】admin登录接口放行（无需JWT）
                .antMatchers("/admin/login").permitAll()
                // 3. 【保留原有】admin接口需ADMIN角色（需JWT认证）
                .antMatchers("/admin/**").hasRole("ADMIN")
                // 4. 【修正逻辑】剩余请求：仅admin未匹配的请求需认证（避免拦截用户接口）
                .anyRequest().authenticated()
                .and()
                // 5. 【保留原有】无状态Session（JWT用）
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 6. 【保留原有】JWT登录过滤器（仅处理/admin/login）
                .addFilterBefore(
                        new JwtLoginFilter(authenticationManagerBean()),
                        UsernamePasswordAuthenticationFilter.class
                )
                // 7. 【保留原有】JWT认证过滤器（仅处理/admin/**接口，用户接口已放行，不会进入）
                .addFilterBefore(
                        new JwtAuthFilter(jwtUtils, userDetailsService()),
                        UsernamePasswordAuthenticationFilter.class);
    }

    // 自定义用户认证逻辑（验证admin用户名密码，不变）
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService())
                .passwordEncoder(passwordEncoder());
    }
}