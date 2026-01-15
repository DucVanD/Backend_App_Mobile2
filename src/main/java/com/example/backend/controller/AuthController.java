package com.example.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import com.example.backend.dto.LoginRequestDto;
import com.example.backend.dto.RegisterRequestDto;
import com.example.backend.dto.UserDto;
import com.example.backend.entity.User;
import com.example.backend.entity.enums.Role;
import com.example.backend.mapper.UserMapper;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtService;
import com.example.backend.service.UserService;

import com.example.backend.repository.PasswordResetTokenRepository;
import com.example.backend.service.EmailService;
import com.example.backend.entity.PasswordResetToken;
import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;
        private final UserRepository userRepository;
        private final UserService userService;
        private final EmailService emailService;
        private final PasswordResetTokenRepository tokenRepository;
        private final PasswordEncoder passwordEncoder;

        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody LoginRequestDto req) {
                try {
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        req.getEmail(),
                                                        req.getPassword()));

                        User user = userRepository.findByEmail(req.getEmail())
                                        .orElseThrow(() -> new RuntimeException("User not found"));

                        String token = jwtService.generateToken(user);

                        return ResponseEntity.ok(
                                        Map.of(
                                                        "token", token,
                                                        "user", UserMapper.toDto(user)));
                } catch (Exception e) {
                        return ResponseEntity.status(401).body(
                                        Map.of("message", "Email hoặc mật khẩu không đúng"));
                }
        }

        @PostMapping("/register")
        public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto req) {

                if (!req.getPassword().equals(req.getConfirmPassword())) {
                        return ResponseEntity.badRequest().body(
                                        Map.of("message", "Password confirmation does not match"));
                }

                UserDto dto = UserDto.builder()
                                .name(req.getName())
                                .email(req.getEmail())
                                .phone(req.getPhone())
                                .address(req.getAddress())
                                .role(Role.USER) // ⚠️ QUAN TRỌNG
                                .status(1)
                                .build();

                UserDto created = userService.create(dto, req.getPassword());
                return ResponseEntity.ok(
                                Map.of(
                                                "status", true,
                                                "user", created));
        }

        @PostMapping("/forgot-password")
        @Transactional
        public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> req) {
                String email = req.get("email");
                User user = userRepository.findByEmail(email).orElse(null);
                if (user == null) {
                        return ResponseEntity.status(404).body(Map.of("message", "Email không tồn tại trong hệ thống"));
                }

                String otp = String.format("%06d", new Random().nextInt(1000000));
                tokenRepository.deleteByEmail(email);

                PasswordResetToken token = PasswordResetToken.builder()
                                .email(email)
                                .token(otp)
                                .expiryDate(LocalDateTime.now().plusMinutes(5))
                                .build();
                tokenRepository.save(token);

                emailService.sendOtpEmail(email, otp);

                return ResponseEntity.ok(Map.of("message", "Mã OTP đã được gửi đến email của bạn"));
        }

        @PostMapping("/verify-code")
        public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> req) {
                String email = req.get("email");
                String code = req.get("code");

                PasswordResetToken token = tokenRepository.findByEmailAndToken(email, code).orElse(null);
                if (token == null || token.isExpired()) {
                        return ResponseEntity.status(400)
                                        .body(Map.of("message", "Mã xác thực không đúng hoặc đã hết hạn"));
                }

                return ResponseEntity.ok(Map.of("status", true, "message", "Mã xác thực hợp lệ"));
        }

        @PostMapping("/reset-password")
        @Transactional
        public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> req) {
                String email = req.get("email");
                String code = req.get("code");
                String newPassword = req.get("newPassword");

                PasswordResetToken token = tokenRepository.findByEmailAndToken(email, code).orElse(null);
                if (token == null || token.isExpired()) {
                        return ResponseEntity.status(400)
                                        .body(Map.of("message", "Mã xác thực không đúng hoặc đã hết hạn"));
                }

                User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);

                tokenRepository.deleteByEmail(email);

                return ResponseEntity.ok(Map.of("status", true, "message", "Thay đổi mật khẩu thành công"));
        }

}
