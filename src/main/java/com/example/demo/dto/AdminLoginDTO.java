package com.example.demo.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data // Lombok自动生成getter/setter
public class AdminLoginDTO {
    @NotBlank(message = "管理员账号不能为空")
    private String username; // 对应数据库admin_user表的username字段

    @NotBlank(message = "密码不能为空")
    private String password; // 对应数据库admin_user表的password字段
}
