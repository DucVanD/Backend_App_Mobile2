package com.example.backend.controller.user;

import com.example.backend.dto.VoucherDto;
import com.example.backend.service.VoucherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {

    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    // 1️⃣ Lấy danh sách voucher
    @GetMapping
    public ResponseEntity<List<VoucherDto>> getAll() {
        return ResponseEntity.ok(voucherService.getAll());
    }

    // 1.5️⃣ Lấy danh sách voucher đang hoạt động
    @GetMapping("/active")
    public ResponseEntity<List<VoucherDto>> getActiveVouchers() {
        return ResponseEntity.ok(voucherService.getActiveVouchers());
    }

    // 1.6️⃣ Kiểm tra và áp dụng voucher
    @GetMapping("/check")
    public ResponseEntity<?> checkVoucher(
            @RequestParam String code,
            @RequestParam java.math.BigDecimal orderAmount) {
        try {
            return ResponseEntity.ok(voucherService.checkVoucher(code, orderAmount));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    java.util.Map.of(
                            "isValid", false,
                            "message", e.getMessage(),
                            "discountAmount", 0));
        }
    }

    // 2️⃣ Lấy voucher theo id
    @GetMapping("/{id}")
    public ResponseEntity<VoucherDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(voucherService.getById(id));
    }

    // 3️⃣ Lấy voucher theo code (dùng khi áp mã)
    @GetMapping("/code/{code}")
    public ResponseEntity<VoucherDto> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(voucherService.getByCode(code));
    }

    // 4️⃣ Tạo voucher mới
    @PostMapping
    public ResponseEntity<VoucherDto> create(@RequestBody VoucherDto dto) {
        return ResponseEntity.ok(voucherService.create(dto));
    }

    // 5️⃣ Cập nhật voucher
    @PutMapping("/{id}")
    public ResponseEntity<VoucherDto> update(
            @PathVariable Integer id,
            @RequestBody VoucherDto dto) {
        return ResponseEntity.ok(voucherService.update(id, dto));
    }

    // 6️⃣ Vô hiệu hóa voucher (soft delete)
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Integer id) {
        voucherService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
