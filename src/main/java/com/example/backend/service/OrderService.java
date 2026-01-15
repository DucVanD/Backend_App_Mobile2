package com.example.backend.service;

import com.example.backend.dto.OrderDto;
import com.example.backend.entity.enums.OrderStatus;
import com.example.backend.entity.enums.PaymentMethod;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {

    List<OrderDto> getAll();

    Page<OrderDto> getPage(int page, int size, String orderCode, OrderStatus status, PaymentMethod paymentMethod);

    OrderDto getById(Integer id);

    OrderDto create(OrderDto dto);

    OrderDto updateStatus(Integer id, OrderDto dto);

    void cancel(Integer id, String reason);

    void delete(Integer id);

    // User order history
    java.util.Map<String, Object> getUserOrders(Integer userId, int page, java.util.Map<String, Object> filters);

    Integer getUserIdByUsername(String username);
}
