package com.example.backend.service;

import com.example.backend.dto.CartDto;

public interface CartService {

    /**
     * Get or create cart for user
     */
    CartDto getOrCreateCart(Integer userId);

    /**
     * Add item to cart (or update quantity if exists)
     */
    CartDto addItem(Integer userId, Integer productId, Integer quantity);

    /**
     * Update cart item quantity
     */
    CartDto updateItemQuantity(Integer userId, Integer cartItemId, Integer quantity);

    /**
     * Remove item from cart
     */
    CartDto removeItem(Integer userId, Integer cartItemId);

    /**
     * Clear all items from cart
     */
    void clearCart(Integer userId);

    /**
     * Get cart by ID (for admin)
     */
    CartDto getCartById(Integer cartId);

    /**
     * Get all carts (for admin)
     */
    java.util.List<CartDto> getAllCarts();
}
