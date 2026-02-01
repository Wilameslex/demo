package com.example.demo.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/test-bcrypt")
    public Boolean testBcrypt() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String plainPwd = "bgs2281037"; // 你输入的明文密码
        String dbEncryptedPwd = "$2b$iYdXFqN9xu7G7dU8vv5WJePNyG68RWGRniPLVejKQJ660p5beu7wW"; // 替换成你覆写后的加密串
        return encoder.matches(plainPwd, dbEncryptedPwd); // 预期返回true
    }
}
