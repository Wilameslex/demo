package com.example.demo.filter;

import com.example.demo.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    // 👇 1. 移除JwtUtils依赖（Token生成逻辑移到Controller，过滤器只做认证）
    // private final JwtUtils jwtUtils;

    // 👇 2. 修正构造器（只保留AuthenticationManager，删除JwtUtils）
    public JwtLoginFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        // 设置登录请求路径（和前端、Controller一致）
        setFilterProcessesUrl("/admin/login");
    }

    // 👇 重写doFilter：先包装请求（缓存请求体），再调用父类逻辑
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 将原始请求包装为「缓存请求体的请求」
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(httpRequest);
        // 传递包装后的请求给过滤器链（后续Controller能读取缓存的请求体）
        super.doFilter(cachedRequest, response, chain);
    }

    // 处理登录请求（验证账号密码）—— 这部分逻辑不变
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // 读取包装后的请求体（不会关闭原始流）
            Map<String, String> loginData = new ObjectMapper().readValue(request.getInputStream(), Map.class);
            String username = loginData.get("username");
            String password = loginData.get("password");

            // 封装认证Token并验证
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new RuntimeException("读取登录请求体失败", e);
        }
    }

    // 👇 3. 重写successfulAuthentication：删除直接写响应的逻辑，放行请求到Controller
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        // 关键：调用chain.doFilter，让请求继续流转到AdminLoginController
        // （之前的代码直接写响应流，导致请求无法到达Controller）
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 👇 4. 优化unsuccessfulAuthentication：返回与Result匹配的错误格式（可选，建议加）
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401未授权
        // 返回与Result结构一致的错误响应，方便前端统一处理
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("code", 401); // 错误状态码
        errorData.put("message", failed.getMessage()); // 错误信息（如“Bad credentials”）
        errorData.put("data", null);
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorData));
    }
}