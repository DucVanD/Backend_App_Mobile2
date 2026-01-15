package com.example.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.backend.entity.User;
import com.example.backend.entity.enums.Role;
import com.example.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // ===== ADMIN =====
        if (!userRepository.existsByEmail("admin@didong.com")) {

            User admin = User.builder()
                    .name("Administrator")
                    .email("admin@didong.com")
                    .phone("0123456789")
                    .address("Hà Nội")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .status(1)
                    .build();

            userRepository.save(admin);

            System.out.println("✅ ADMIN account created");
            System.out.println("   Email: admin@didong.com");
            System.out.println("   Password: admin123");
        }
    }
}
