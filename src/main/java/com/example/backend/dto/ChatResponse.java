package com.example.backend.dto;

import java.util.List;

public class ChatResponse {
    private String message;
    private List<ProductDto> products; // Optional: when AI suggests products
    private String type; // "text" or "products"

    public ChatResponse() {
    }

    public ChatResponse(String message) {
        this.message = message;
        this.type = "text";
    }

    public ChatResponse(String message, List<ProductDto> products) {
        this.message = message;
        this.products = products;
        this.type = "products";
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ProductDto> getProducts() {
        return products;
    }

    public void setProducts(List<ProductDto> products) {
        this.products = products;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
