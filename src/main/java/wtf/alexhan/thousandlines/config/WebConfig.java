package wtf.alexhan.thousandlines.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import wtf.alexhan.thousandlines.interceptor.MaintenanceInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir}")
    private String uploadDir;
    
    @Autowired
    private MaintenanceInterceptor maintenanceInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");

    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(maintenanceInterceptor)
                .addPathPatterns("/**") // 拦截所有路径
                .excludePathPatterns(
                    "/maintenance", // 排除维护页面本身
                    "/css/**",      // 排除静态资源
                    "/js/**", 
                    "/images/**",
                    "/uploads/**"
                );
    }
}