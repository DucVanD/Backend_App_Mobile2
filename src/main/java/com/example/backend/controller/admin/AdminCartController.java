package com.example.backend.controller.admin;

import com.example.backend.dto.CartDto;
import com.example.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/carts")
@RequiredArgsConstructor
public class AdminCartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<List<CartDto>> getAllCarts() {
        List<CartDto> carts = cartService.getAllCarts();
        return ResponseEntity.ok(carts);
    }

    @GetMapping("/page")
    public ResponseEntity<org.springframework.data.domain.Page<CartDto>> getCartsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        // For now, return all carts wrapped in a Page object
        List<CartDto> allCarts = cartService.getAllCarts();

        // Manual pagination
        int start = page * size;
        int end = Math.min(start + size, allCarts.size());
        List<CartDto> pageContent = start < allCarts.size()
                ? allCarts.subList(start, end)
                : java.util.Collections.emptyList();

        @SuppressWarnings("null")
        org.springframework.data.domain.Page<CartDto> pageResult = new org.springframework.data.domain.PageImpl<>(
                pageContent,
                org.springframework.data.domain.PageRequest.of(page, size),
                allCarts.size());

        return ResponseEntity.ok(pageResult);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartDto> getCartById(@PathVariable Integer id) {
        CartDto cart = cartService.getCartById(id);
        return ResponseEntity.ok(cart);
    }
}
