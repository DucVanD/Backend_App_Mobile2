package com.example.backend.service;

import com.example.backend.dto.UserDto;

import java.util.List;

public interface UserService {

        List<UserDto> getAll();

        UserDto getById(Integer id);

        UserDto getByEmail(String email);

        UserDto create(UserDto dto, String rawPassword);

        UserDto update(Integer id, UserDto dto);

        void delete(Integer id);

        org.springframework.data.domain.Page<UserDto> getPage(
                        java.util.List<com.example.backend.entity.enums.Role> roles,
                        int page, int size);

        org.springframework.data.domain.Page<UserDto> search(String keyword,
                        java.util.List<com.example.backend.entity.enums.Role> roles, int page, int size);

        void lock(Integer id);

        void unlock(Integer id);

        Integer getUserIdByUsername(String username);
}
