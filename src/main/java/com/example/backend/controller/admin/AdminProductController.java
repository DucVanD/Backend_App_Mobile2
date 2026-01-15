package com.example.backend.controller.admin;

import com.example.backend.dto.ProductDto;
import com.example.backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor

@PreAuthorize("hasRole('ADMIN')") // 🔒 CHỈ ADMIN
public class AdminProductController {

    private final ProductService productService;

    // ==================
    // READ
    // ==================
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAll() {
        return ResponseEntity.ok(productService.getAll());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<ProductDto>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        return ResponseEntity.ok(productService.getPage(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductDto> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getBySlug(slug));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        return ResponseEntity.ok(productService.search(keyword, page, size));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<ProductDto>> filter(
            @RequestParam(required = false) List<Integer> categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean hasPromotion,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                productService.filter(categoryId, status, minPrice, maxPrice, keyword, hasPromotion, sortBy, page,
                        size));
    }

    // ==================
    // WRITE
    // ==================
    @PostMapping
    public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> update(
            @PathVariable Integer id,
            @Valid @RequestBody ProductDto dto) {
        return ResponseEntity.ok(productService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        productService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Xóa sản phẩm thành công"));
    }

    @PutMapping("/toggle-status/{id}")
    public ResponseEntity<Void> toggleStatus(@PathVariable Integer id) {
        productService.toggleStatus(id);
        return ResponseEntity.ok().build();
    }
}
