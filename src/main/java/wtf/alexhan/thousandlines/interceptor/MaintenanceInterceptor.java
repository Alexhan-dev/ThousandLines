package wtf.alexhan.thousandlines.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import wtf.alexhan.thousandlines.config.MaintenanceConfig;

@Component
public class MaintenanceInterceptor implements HandlerInterceptor {
    
    @Autowired
    private MaintenanceConfig maintenanceConfig;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果维护模式启用，且不是访问维护页面本身，则重定向到维护页面
        if (maintenanceConfig.isEnabled() && !request.getRequestURI().equals("/maintenance")) {
            response.sendRedirect("/maintenance");
            return false;
        }
        return true;
    }
}