package com.example.backend.controller.admin;

import com.example.backend.dto.OrderDto;
import com.example.backend.entity.enums.OrderStatus;
import com.example.backend.entity.enums.PaymentMethod;
import com.example.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // 🔒 CHỈ ADMIN
public class AdminOrderController {

    private final OrderService orderService;

    // ==================
    // READ
    // ==================
    @GetMapping
    public ResponseEntity<List<OrderDto>> getAll() {
        return ResponseEntity.ok(orderService.getAll());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<OrderDto>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PaymentMethod paymentMethod) {
        return ResponseEntity.ok(orderService.getPage(page, size, orderCode, status, paymentMethod));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    // ==================
    // WRITE
    // ==================
    @PutMapping("/{id}")
    public ResponseEntity<OrderDto> update(
            @PathVariable Integer id,
            @RequestBody OrderDto dto) {
        // Admin can update order status, payment status, etc.
        return ResponseEntity.ok(orderService.updateStatus(id, dto));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(
            @PathVariable Integer id,
            @RequestParam String reason) {
        orderService.cancel(id, reason);
        return ResponseEntity.ok(Map.of("status", true, "message", "Hủy đơn hàng thành công"));
    }
}
