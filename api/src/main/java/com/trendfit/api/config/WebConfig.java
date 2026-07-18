package com.trendfit.api.config;

import com.trendfit.api.modules.user.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Áp dụng Interceptor cho các đường dẫn quản lý đơn hàng
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/admin/orders/**");
    }
}