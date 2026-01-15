package com.example.backend.controller.admin;

import com.example.backend.dto.UserDto;
import com.example.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor

@PreAuthorize("hasRole('ADMIN')") // 🔒 CHỈ ADMIN
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll();
    }

    @GetMapping("/page")
    public ResponseEntity<Page<UserDto>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<com.example.backend.entity.enums.Role> roles) {

        if (keyword != null && !keyword.isBlank()) {
            return ResponseEntity.ok(userService.search(keyword, roles, page, size));
        }
        return ResponseEntity.ok(userService.getPage(roles, page, size));
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable Integer id) {
        return userService.getById(id);
    }

    @PostMapping
    public UserDto create(
            @RequestParam String password,
            @Valid @RequestBody UserDto dto) {
        return userService.create(dto, password);
    }

    @PutMapping("/{id}")
    public UserDto update(
            @PathVariable Integer id,
            @Valid @RequestBody UserDto dto) {
        return userService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        userService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Xóa người dùng thành công"));
    }

    @PutMapping("/{id}/lock")
    public ResponseEntity<?> lock(@PathVariable Integer id) {
        userService.lock(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/unlock")
    public ResponseEntity<?> unlock(@PathVariable Integer id) {
        userService.unlock(id);
        return ResponseEntity.ok().build();
    }
}
