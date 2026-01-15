package com.example.backend.entity;

import jakarta.persistence.*;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import com.example.backend.entity.enums.Role;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Khuyên dùng Long thay vì Integer cho User ID

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email; // Dùng làm tài khoản đăng nhập


    @Column(nullable = false)
    private String password; // Lưu hash password (BCrypt)

    @Column(unique = true, length = 20, nullable = false)
    private String phone;

    @Column(length = 255)
    private String address;

    private String avatar;

    @Column(name = "avatar_public_id")
    private String avatarPublicId;

    @Enumerated(EnumType.STRING) // Lưu "ADMIN" vào database (dạng chữ)
    @Column(name = "role", nullable = false, length = 20)
    private Role role; // Kiểu dữ liệu là Role (Enum)

    @Builder.Default
    @Column(nullable = false)
    private Integer status = 1; // 1: Active, 0: Locked

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}