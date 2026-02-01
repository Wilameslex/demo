package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.AdminUser;

public interface AdminUserService extends IService<AdminUser> {
    // 根据用户名查询管理员（用于登录验证）
    AdminUser getByUsername(String username);

    // 更新最后登录时间
    void updateLastLoginTime(String username);
}