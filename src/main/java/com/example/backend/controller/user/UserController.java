package com.example.backend.controller.user;

import com.example.backend.dto.UserDto;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PostMapping
    public ResponseEntity<UserDto> create(
            @RequestBody UserDto dto,
            @RequestParam String password) {
        return ResponseEntity.ok(userService.create(dto, password));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(
            @PathVariable Integer id,
            @RequestBody UserDto dto) {
        return ResponseEntity.ok(userService.update(id, dto));
    }

    @PutMapping("/{id}/lock")
    public ResponseEntity<Void> lock(@PathVariable Integer id) {
        userService.lock(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/unlock")
    public ResponseEntity<Void> unlock(@PathVariable Integer id) {
        userService.unlock(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(
            @PathVariable Integer id,
            @RequestBody Map<String, String> req) {
        String currentPassword = req.get("currentPassword");
        String newPassword = req.get("newPassword");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.status(400).body(Map.of("message", "Mật khẩu hiện tại không đúng"));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("status", true, "message", "Đổi mật khẩu thành công"));
    }
}
