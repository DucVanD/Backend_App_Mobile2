package com.example.backend.mapper;

import com.example.backend.dto.UserDto;
import com.example.backend.entity.User;

public class UserMapper {

    // Entity → DTO
    public static UserDto toDto(User user) {
        if (user == null)
            return null;

        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .avatar(user.getAvatar())
                .avatarPublicId(user.getAvatarPublicId())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    // DTO → Entity (CREATE)
    // password xử lý riêng (encode)
    public static User toEntity(UserDto dto, String encodedPassword) {
        if (dto == null)
            return null;

        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .avatar(dto.getAvatar())
                .avatarPublicId(dto.getAvatarPublicId())
                .role(dto.getRole())
                .status(dto.getStatus())
                .password(encodedPassword)
                .build();
    }
}
