package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("admin_user") // 必须和数据库表名一致
public class AdminUser {
    @TableId(type = IdType.AUTO) // 主键自增
    private Long id; // 对应表中id字段

    private String username; // 管理员账号（唯一）

    private String password; // 加密后的密码（BCrypt）

    private String realName; // 真实姓名（可选）

    private Integer status; // 状态：1-启用，0-禁用

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime lastLoginTime; // 最后登录时间
}
