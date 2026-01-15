package com.example.backend.controller.user;

import com.example.backend.dto.CartDto;
import com.example.backend.service.CartService;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<CartDto> getCart(Authentication authentication) {
        Integer userId = getUserIdFromAuth(authentication);
        CartDto cart = cartService.getOrCreateCart(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItem(
            @RequestParam Integer productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            Authentication authentication) {
        Integer userId = getUserIdFromAuth(authentication);
        CartDto cart = cartService.addItem(userId, productId, quantity);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDto> updateItemQuantity(
            @PathVariable Integer itemId,
            @RequestParam Integer quantity,
            Authentication authentication) {
        Integer userId = getUserIdFromAuth(authentication);
        CartDto cart = cartService.updateItemQuantity(userId, itemId, quantity);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDto> removeItem(
            @PathVariable Integer itemId,
            Authentication authentication) {
        Integer userId = getUserIdFromAuth(authentication);
        CartDto cart = cartService.removeItem(userId, itemId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        Integer userId = getUserIdFromAuth(authentication);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    private Integer getUserIdFromAuth(Authentication authentication) {
        String username = authentication.getName();
        return userService.getUserIdByUsername(username);
    }
}
