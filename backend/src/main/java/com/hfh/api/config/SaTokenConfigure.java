package com.hfh.api.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 启用注解鉴权（全局拦截所有路径）
        // registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");

        // 1. 注册 Sa-Token 拦截器
        registry.addInterceptor(new SaInterceptor(handler -> {
            SaRouter.match("/**")
                    // 2. 一次性排除 Knife4j 全部路径
                    .notMatch(
                            "/doc.html",
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/swagger-resources/**",
                            "/v3/api-docs/**",
                            "/v2/api-docs/**",
                            "/webjars/**",
                            "/swagger-resources/configuration/ui",
                            "/swagger-resources/configuration/security",
                            "/favicon.ico"
                    )
                    .notMatch("/auth/login", "/auth/refresh", "/auth/register", "/system/config/list", "/captcha/**")
                    // 3. 其余接口必须登录
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }

}
