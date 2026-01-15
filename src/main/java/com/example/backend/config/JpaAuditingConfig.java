package com.example.backend.config;

import com.example.backend.security.CustomUserDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    @Bean
    @SuppressWarnings("null") // ✅ Thêm dòng này để tắt cảnh báo vàng của IDE
    public AuditorAware<Integer> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || 
                !authentication.isAuthenticated() || 
                authentication.getPrincipal().equals("anonymousUser")) {
                return Optional.empty();
            }

            Object principal = authentication.getPrincipal();
            
            if (principal instanceof CustomUserDetails) {
                Integer userId = ((CustomUserDetails) principal).getId();
                // ✅ Dùng ofNullable an toàn hơn (phòng trường hợp ID bị null)
                return Optional.ofNullable(userId); 
            }

            return Optional.empty();
        };
    }
}