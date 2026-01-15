package com.example.backend.security;

import com.example.backend.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // --- SỬA LỖI 1: Ép kiểu Long về Integer để khớp với Product ---
    public Integer getId() {
        return user.getId() != null ? user.getId().intValue() : null;
    }

    // --- SỬA LỖI 2: Trả về Email thay vì Username ---
    @Override
    public String getUsername() {
        return user.getEmail(); 
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // (Tùy chọn) Map Role từ String sang Authority
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // user.getRole().name() sẽ trả về "ADMIN", "STAFF" hoặc "CUSTOMER"
        if (user.getRole() != null) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return user.getStatus() == 1; } // Check status active
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}