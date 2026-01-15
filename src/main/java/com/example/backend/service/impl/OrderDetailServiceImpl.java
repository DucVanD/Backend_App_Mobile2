package com.example.backend.service.impl;

import com.example.backend.dto.OrderDetailDto;
import com.example.backend.entity.Order;
import com.example.backend.entity.OrderDetail;
import com.example.backend.entity.Product;
import com.example.backend.mapper.OrderDetailMapper;
import com.example.backend.repository.OrderDetailRepository;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.ProductRepository;
import com.example.backend.service.OrderDetailService;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class OrderDetailServiceImpl implements OrderDetailService {

        private final OrderDetailRepository orderDetailRepository;
        private final OrderRepository orderRepository;
        private final ProductRepository productRepository;

        @Override
        public List<OrderDetailDto> getByOrderId(Integer orderId) {

                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Order not found"));

                return orderDetailRepository.findByOrder(order)
                                .stream()
                                .map(OrderDetailMapper::toDto)
                                .collect(Collectors.toList());
        }

        @Override
        public OrderDetailDto create(OrderDetailDto dto) {

                Order order = orderRepository.findById(dto.getOrderId())
                                .orElseThrow(() -> new RuntimeException("Order not found"));

                Product product = productRepository.findById(dto.getProductId())
                                .orElseThrow(() -> new RuntimeException("Product not found"));

                OrderDetail detail = OrderDetailMapper.toEntity(dto, order, product);

                OrderDetail saved = orderDetailRepository.save(detail);
                return OrderDetailMapper.toDto(saved);
        }
}
