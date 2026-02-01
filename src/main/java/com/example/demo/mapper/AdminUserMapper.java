package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
    // 根据用户名查询管理员（用于登录验证）
    AdminUser selectByUsername(@Param("username") String username);

    // 更新最后登录时间
    int updateLastLoginTime(@Param("username") String username, @Param("lastLoginTime") LocalDateTime lastLoginTime);
}
