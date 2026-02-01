package com.example.demo.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.util.JwtUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; // 正确导入Apache Commons Logging

public class JwtAuthFilter extends OncePerRequestFilter {

    // 1. 正确初始化Apache Commons Log（避免日志对象为null）
    private static final Log logger = LogFactory.getLog(JwtAuthFilter.class);

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    // 构造器注入（保持不变）
    public JwtAuthFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 从请求头获取Token（前端传递的Header：Admin-Token）
        final String authHeader = request.getHeader("Admin-Token");
        final String username;
        final String jwtToken;

        // 无Token则跳过验证（保持不变）
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 提取Token（去掉Bearer前缀）
        jwtToken = authHeader.substring(7);
        try {
            // 从Token提取用户名
            username = jwtUtils.extractUsername(jwtToken);

            // 用户名存在且未认证
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 从数据库加载用户信息
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 验证Token有效性
                if (jwtUtils.validateToken(jwtToken, userDetails)) {
                    // 构建认证Token并放入SecurityContext
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // 2. 修正日志：Apache Commons Logging不支持多参数，改为字符串拼接
                    logger.info("Token验证成功，用户: " + username + "，权限: " + userDetails.getAuthorities());
                }
            }
        } catch (io.jsonwebtoken.SignatureException e) {
            // 3. 修正日志：error()第二个参数传Throwable（e），而非String
            logger.error("Token签名无效（密钥不匹配或Token篡改）: " + e.getMessage(), e);
            sendErrorResponse(response, 401, "Token无效或已篡改");
            return;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.error("Token已过期: " + e.getMessage(), e);
            sendErrorResponse(response, 401, "Token已过期，请重新登录");
            return;
        } catch (Exception e) {
            // 4. 统一异常日志格式：先拼接消息，再传异常对象
            logger.error("Token验证失败: " + e.getMessage(), e);
            sendErrorResponse(response, 401, "Token验证失败");
            return;
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }

    // 5. 修正sendErrorResponse：确保无异常抛出错误（返回响应后无需throw）
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        Map<String, Object> error = new HashMap<>();
        error.put("code", status);
        error.put("message", message);
        error.put("data", null);
        // 用Jackson写响应（确保导入com.fasterxml.jackson.databind.ObjectMapper）
        new ObjectMapper().writeValue(response.getWriter(), error);
    }
}