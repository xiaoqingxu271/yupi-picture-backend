package com.yupi.yupicturebackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
* @author chun0
* @since 2025/11/7 16:54
* @version 1.0
*/
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 跨域配置
     *
     * @param registry 跨域注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 覆盖所有的请求路径
        registry.addMapping("/**")
                // 允许发送 Cookie
                .allowCredentials(true)
                // 放行任意域名
                .allowedOriginPatterns("*")
                // 允许所有方法类型
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许携带任意头信息
                .allowedHeaders("*")
                // 暴露所有头信息
                .exposedHeaders("*");
    }
}
