package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.AdminLoginDTO;
import com.example.demo.service.AdminUserService;
import com.example.demo.util.JwtUtils; // 1. 修正包名：util→utils（带s）
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminLoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired // 2. 注入AdminUserService（之前遗漏，会导致空指针）
    private AdminUserService adminUserService;

    @Autowired // 3. 注入JwtUtils（之前未注入，且调用方式错误）
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result<?> login(@RequestBody AdminLoginDTO loginDTO) {
        // 验证账号密码
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );

        // 生成Token：修正为实例方法调用，且传递UserDetails（不是String）
        UserDetails userDetails = (UserDetails) authentication.getPrincipal(); // 获取认证后的用户信息
        String token = jwtUtils.generateToken(userDetails); // 4. 实例方法调用（之前误写为静态方法）

        // 更新最后登录时间
        adminUserService.updateLastLoginTime(userDetails.getUsername());

        return Result.success(Map.of("token", token, "username", userDetails.getUsername()));
    }
}