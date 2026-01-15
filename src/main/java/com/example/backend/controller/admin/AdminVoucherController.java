package com.example.backend.controller.admin;

import com.example.backend.dto.VoucherDto;
import com.example.backend.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminVoucherController {

    private final VoucherService voucherService;

    // 📋 Get all vouchers
    @GetMapping
    public ResponseEntity<List<VoucherDto>> getAll() {
        return ResponseEntity.ok(voucherService.getAll());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<VoucherDto>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(voucherService.getPage(page, size));
    }

    // 🔍 Get voucher by ID
    @GetMapping("/{id}")
    public ResponseEntity<VoucherDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(voucherService.getById(id));
    }

    // ➕ Create new voucher
    @PostMapping
    public ResponseEntity<VoucherDto> create(@Valid @RequestBody VoucherDto dto) {
        return ResponseEntity.ok(voucherService.create(dto));
    }

    // ✏️ Update voucher
    @PutMapping("/{id}")
    public ResponseEntity<VoucherDto> update(
            @PathVariable Integer id,
            @Valid @RequestBody VoucherDto dto) {
        return ResponseEntity.ok(voucherService.update(id, dto));
    }

    // 🗑️ Delete voucher
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        voucherService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
