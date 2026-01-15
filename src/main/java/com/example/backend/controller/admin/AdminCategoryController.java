package com.example.backend.controller.admin;

import com.example.backend.dto.CategoryDto;
import com.example.backend.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor

@PreAuthorize("hasRole('ADMIN')") // 🔒 CHỈ ADMIN
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<CategoryDto>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        return ResponseEntity.ok(categoryService.getPage(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CategoryDto> create(
            @Valid @RequestBody CategoryDto dto) {
        return ResponseEntity.ok(categoryService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> update(
            @PathVariable Integer id,
            @Valid @RequestBody CategoryDto dto) {
        return ResponseEntity.ok(categoryService.update(id, dto));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id) {
        categoryService.toggleStatus(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            categoryService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Xóa danh mục thành công"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Lỗi: " + e.getMessage()));
        }
    }
}
