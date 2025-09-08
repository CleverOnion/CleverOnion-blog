package com.cleveronion.blog.infrastructure.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 * 配置 Sa-Token 的拦截器和路由规则
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    

    
    /**
     * 注册拦截器
     * 
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 指定一条 match 规则
            SaRouter
                // 拦截所有路由
                .match("/**")
                // 排除 OPTIONS 预检请求（CORS 跨域预检）
                .notMatchMethod("OPTIONS")
                // 排除所有 GET 请求（查询接口不需要认证）
                .notMatchMethod("GET")
                // 排除登录接口
                .notMatch("/auth/login/github")
                // 排除 OAuth2 回调接口
                .notMatch("/auth/callback/github")
                .notMatch("/auth/callback/**")
                // 排除管理员检查接口（内部会检查登录状态）
                .notMatch("/admin/check")
                // 排除健康检查接口
                .notMatch("/api/health/**")
                // 排除静态资源
                .notMatch("/static/**")
                // 排除 Swagger 文档
                .notMatch("/swagger-ui/**")
                .notMatch("/v3/api-docs/**")
                .notMatch("/swagger-resources/**")
                .notMatch("/webjars/**")
                // 排除网站图标
                .notMatch("/favicon.ico")
                // 排除错误页面
                .notMatch("/error")
                // 排除监控端点
                .notMatch("/actuator/**")
                // 执行认证函数
                .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }
}