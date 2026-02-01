package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 原有/api/**配置
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
        // 1. 放行用户接口（匿名可访问）
        registry.addMapping("/genes/**")
                .allowedOrigins("*") // 生产环境改为前端域名（如http://localhost:3001）
                .allowedMethods("GET", "POST", "OPTIONS") // 必须包含OPTIONS（处理预检请求）
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);

        registry.addMapping("/pathway/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
        registry.addMapping("/enrichment/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
        registry.addMapping("/gene/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);

        // 2. 放行admin接口（需认证）
        registry.addMapping("/admin/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);

        // 3. 放行其他用户接口（如表达搜索、基因网络等）
        registry.addMapping("/expression/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
        registry.addMapping("/gene-network/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
        registry.addMapping("/genome/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "OPTIONS")  // 仅需GET和预检请求
                .allowedHeaders("*")
                .maxAge(3600);
        registry.addMapping("/mitochondrion/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "OPTIONS")  // 仅需GET和预检请求
                .allowedHeaders("*")
                .maxAge(3600);
        registry.addMapping("/overview/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
        registry.addMapping("/phenotype/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "OPTIONS") // 仅需GET（查询）和OPTIONS（预检）
                .allowedHeaders("*")
                .maxAge(3600);
        registry.addMapping("/phenotype/total/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
