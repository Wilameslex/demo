package com.example.demo;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// 普通Java类，无需依赖Spring容器，直接运行main方法
public class BCryptGenerator {
    public static void main(String[] args) {
        // 1. 实例化BCrypt密码编码器（和SecurityConfig中完全一样）
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 2. 替换为你要加密的明文密码（比如你的管理员密码bgs2281037）
        String plainPassword1 = "bgs2281037"; // WLx的密码
        String plainPassword2 = "Crayfish123"; // WJ的密码

        // 3. 生成加密串（每次结果不同，但验证时都能匹配）
        String encryptedPwd1 = encoder.encode(plainPassword1);
        String encryptedPwd2 = encoder.encode(plainPassword2);

        // 4. 打印结果，复制到数据库中
        System.out.println("明文密码【" + plainPassword1 + "】的BCrypt加密串：");
        System.out.println(encryptedPwd1);
        System.out.println("----------------------------------------");
        System.out.println("明文密码【" + plainPassword2 + "】的BCrypt加密串：");
        System.out.println(encryptedPwd2);
    }
}
