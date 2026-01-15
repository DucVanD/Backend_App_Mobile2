package com.example.backend.service;

import com.example.backend.dto.VoucherDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface VoucherService {

    List<VoucherDto> getAll();

    List<VoucherDto> getActiveVouchers();

    Page<VoucherDto> getPage(int page, int size);

    VoucherDto getById(Integer id);

    VoucherDto getByCode(String code);

    VoucherDto create(VoucherDto dto);

    VoucherDto update(Integer id, VoucherDto dto);

    void deactivate(Integer id);

    void delete(Integer id);

    void incrementUsage(String voucherCode);

    com.example.backend.dto.VoucherCheckResponse checkVoucher(String code, java.math.BigDecimal orderAmount);
}
