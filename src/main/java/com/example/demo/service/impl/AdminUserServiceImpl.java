package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.AdminUser;
import com.example.demo.mapper.AdminUserMapper;
import com.example.demo.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser> implements AdminUserService {

    private final AdminUserMapper adminUserMapper;

    // 根据用户名查询管理员
    @Override
    public AdminUser getByUsername(String username) {
        return adminUserMapper.selectByUsername(username);
    }

    // 更新最后登录时间
    @Override
    public void updateLastLoginTime(String username) {
        adminUserMapper.updateLastLoginTime(username, LocalDateTime.now());
    }
}
