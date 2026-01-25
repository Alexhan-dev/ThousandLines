package wtf.alexhan.thousandlines.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "maintenance")
public class MaintenanceConfig {
    
    private boolean enabled = false;
    private String message = "系统维护中，请稍后再试";
    private String estimatedCompletionTime = "2-3小时";
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getEstimatedCompletionTime() {
        return estimatedCompletionTime;
    }
    
    public void setEstimatedCompletionTime(String estimatedCompletionTime) {
        this.estimatedCompletionTime = estimatedCompletionTime;
    }
}