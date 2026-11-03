package com.hiresense.config;

import com.hiresense.service.ActivityLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ActivityAuditInterceptor implements WebMvcConfigurer, HandlerInterceptor {
    private final ActivityLogService audit;
    public ActivityAuditInterceptor(ActivityLogService audit) { this.audit = audit; }

    @Override
    public void addInterceptors(InterceptorRegistry registry) { registry.addInterceptor(this).addPathPatterns("/api/**"); }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        if (response.getStatus() >= 400) return;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) return;
        try {
            audit.record(authentication.getName(), request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), response.getStatus());
        } catch (RuntimeException ignored) {
            // Audit failures must not turn a completed user request into a server error.
        }
    }
}
