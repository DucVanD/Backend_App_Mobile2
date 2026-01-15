package com.example.backend.service.impl;

import com.example.backend.dto.CartDto;
import com.example.backend.entity.Cart;
import com.example.backend.entity.CartItem;
import com.example.backend.entity.Product;
import com.example.backend.entity.User;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.mapper.CartMapper;
import com.example.backend.repository.CartItemRepository;
import com.example.backend.repository.CartRepository;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public CartDto getOrCreateCart(Integer userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCartForUser(userId));
        return CartMapper.toDto(cart);
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public CartDto addItem(Integer userId, Integer productId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCartForUser(userId));

        if (productId == null) {
            throw new IllegalArgumentException("Product ID must not be null");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Check if item already exists in cart
        CartItem existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);

        if (existingItem != null) {
            // Update quantity
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepository.save(existingItem);
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
            if (newItem == null) {
                throw new IllegalStateException("Failed to create CartItem");
            }
            cart.addItem(newItem);
            cartItemRepository.save(newItem);
        }

        cart = cartRepository.save(cart);
        return CartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public CartDto updateItemQuantity(Integer userId, Integer cartItemId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        if (cartItemId == null) {
            throw new IllegalArgumentException("Cart item ID must not be null");
        }
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        // Verify item belongs to user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to user's cart");
        }

        if (quantity <= 0) {
            cart.removeItem(cartItem);
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        cart = cartRepository.save(cart);
        return CartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public CartDto removeItem(Integer userId, Integer cartItemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        if (cartItemId == null) {
            throw new IllegalArgumentException("Cart item ID must not be null");
        }
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        // Verify item belongs to user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to user's cart");
        }

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);
        cart = cartRepository.save(cart);

        return CartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public void clearCart(Integer userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        cartItemRepository.deleteByCartId(cart.getId());
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartDto getCartById(Integer cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));
        return CartMapper.toDto(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartDto> getAllCarts() {
        return cartRepository.findAll().stream()
                .map(CartMapper::toDto)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("null")
    private Cart createCartForUser(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Cart cart = Cart.builder()
                .user(user)
                .build();
        return cartRepository.save(cart);
    }
}
