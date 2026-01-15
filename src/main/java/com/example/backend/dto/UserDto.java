package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.example.backend.entity.enums.Role;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private Integer id;

    @NotBlank(message = "Tên người dùng không được để trống")
    private String name;

    @NotBlank(message = "Email không được để trống")
    @jakarta.validation.constraints.Email(message = "Email không đúng định dạng")
    private String email;
    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    private String address;
    private String avatar;
    private String avatarPublicId;

    @NotNull(message = "Vai trò không được để trống")
    private Role role;
    private Integer status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
