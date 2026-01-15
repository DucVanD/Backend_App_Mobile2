package com.example.backend.service.impl;

import com.example.backend.dto.VoucherDto;
import com.example.backend.entity.Voucher;
import com.example.backend.mapper.VoucherMapper;
import com.example.backend.repository.VoucherRepository;
import com.example.backend.service.VoucherService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;

    @Override
    public List<VoucherDto> getAll() {
        return voucherRepository.findAll()
                .stream()
                .map(VoucherMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VoucherDto> getActiveVouchers() {
        return voucherRepository.findActiveVouchers()
                .stream()
                .map(VoucherMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<VoucherDto> getPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return voucherRepository.findAll(pageable)
                .map(VoucherMapper::toDto);
    }

    @Override
    public VoucherDto getById(Integer id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
        return VoucherMapper.toDto(voucher);
    }

    @Override
    public VoucherDto getByCode(String code) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
        return VoucherMapper.toDto(voucher);
    }

    @Override
    public VoucherDto create(VoucherDto dto) {
        // 1. Validate Code Duplication
        if (voucherRepository.existsByCode(dto.getVoucherCode())) {
            throw new IllegalArgumentException("voucherCode:Mã voucher đã tồn tại");
        }

        // 2. Validate Name Duplication
        if (voucherRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("name:Tên voucher đã tồn tại");
        }

        // 3. Validate Date
        if (dto.getStartDate() != null && dto.getEndDate() != null && dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new IllegalArgumentException("startDate:Ngày bắt đầu phải trước ngày kết thúc");
        }

        Voucher voucher = VoucherMapper.toEntity(dto);
        Voucher saved = voucherRepository.save(voucher);
        return VoucherMapper.toDto(saved);
    }

    @Override
    public VoucherDto update(Integer id, VoucherDto dto) {

        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        // 1. Validate Code Duplication
        if (voucherRepository.existsByCodeAndIdNot(dto.getVoucherCode(), id)) {
            throw new IllegalArgumentException("voucherCode:Mã voucher đã tồn tại");
        }

        // 2. Validate Name Duplication
        if (voucherRepository.existsByNameAndIdNot(dto.getName(), id)) {
            throw new IllegalArgumentException("name:Tên voucher đã tồn tại");
        }

        // 3. Validate Date
        if (dto.getStartDate() != null && dto.getEndDate() != null && dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new IllegalArgumentException("startDate:Ngày bắt đầu phải trước ngày kết thúc");
        }

        voucher.setCode(dto.getVoucherCode());
        voucher.setName(dto.getName());
        voucher.setDescription(dto.getDescription());
        voucher.setDiscountType(dto.getDiscountType());

        voucher.setDiscountValue(dto.getDiscountValue());
        voucher.setMaxDiscount(dto.getMaxDiscount());
        voucher.setMinOrderAmount(dto.getMinOrderAmount());
        voucher.setUsageLimit(dto.getUsageLimit());
        voucher.setStartDate(dto.getStartDate());
        voucher.setEndDate(dto.getEndDate());
        voucher.setStatus(dto.getStatus());

        Voucher updated = voucherRepository.save(voucher);
        return VoucherMapper.toDto(updated);
    }

    @Override
    public void deactivate(Integer id) {

        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        voucher.setStatus(0); // Inactive
        voucher.setEndDate(LocalDateTime.now());

        voucherRepository.save(voucher);
    }

    @Override
    public void delete(Integer id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        // Soft delete
        voucher.setDeletedAt(LocalDateTime.now());
        voucherRepository.save(voucher);
    }

    @Override
    public void incrementUsage(String voucherCode) {
        Voucher voucher = voucherRepository.findByCode(voucherCode)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        Integer currentCount = voucher.getUsedCount();
        if (currentCount == null) {
            currentCount = 0;
        }
        voucher.setUsedCount(currentCount + 1);
        voucherRepository.save(voucher);
    }

    @Override
    public com.example.backend.dto.VoucherCheckResponse checkVoucher(String code, java.math.BigDecimal orderAmount) {
        // Find voucher by code
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không tồn tại"));

        // Check if voucher is active
        if (voucher.getStatus() == 0) {
            throw new IllegalArgumentException("Mã giảm giá đã bị vô hiệu hóa");
        }

        // Check if voucher is deleted
        if (voucher.getDeletedAt() != null) {
            throw new IllegalArgumentException("Mã giảm giá không còn khả dụng");
        }

        // Check date validity
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getStartDate())) {
            throw new IllegalArgumentException("Mã giảm giá chưa đến thời gian sử dụng");
        }
        if (now.isAfter(voucher.getEndDate())) {
            throw new IllegalArgumentException("Mã giảm giá đã hết hạn");
        }

        // Check usage limit
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng");
        }

        // Check minimum order amount
        if (orderAmount.compareTo(voucher.getMinOrderAmount()) < 0) {
            throw new IllegalArgumentException(
                    String.format("Đơn hàng tối thiểu %s để sử dụng mã này",
                            voucher.getMinOrderAmount().toString()));
        }

        // Calculate discount
        java.math.BigDecimal discountAmount;
        if (voucher.getDiscountType() == com.example.backend.entity.enums.DiscountType.PERCENTAGE) {
            // Percentage discount
            discountAmount = orderAmount.multiply(voucher.getDiscountValue())
                    .divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

            // Apply max discount if set
            if (voucher.getMaxDiscount() != null && discountAmount.compareTo(voucher.getMaxDiscount()) > 0) {
                discountAmount = voucher.getMaxDiscount();
            }
        } else {
            // Fixed amount discount
            discountAmount = voucher.getDiscountValue();

            // Discount cannot exceed order amount
            if (discountAmount.compareTo(orderAmount) > 0) {
                discountAmount = orderAmount;
            }
        }

        // Return success response
        return com.example.backend.dto.VoucherCheckResponse.builder()
                .isValid(true)
                .discountAmount(discountAmount)
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .maxDiscountAmount(voucher.getMaxDiscount())
                .minOrderAmount(voucher.getMinOrderAmount())
                .voucherCode(voucher.getCode())
                .message("Áp dụng mã giảm giá thành công")
                .build();
    }
}
