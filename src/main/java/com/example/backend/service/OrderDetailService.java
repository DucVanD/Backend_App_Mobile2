package com.example.backend.service;

import com.example.backend.dto.OrderDetailDto;

import java.util.List;

public interface OrderDetailService {

    List<OrderDetailDto> getByOrderId(Integer orderId);
   
    OrderDetailDto create(OrderDetailDto dto);
}
