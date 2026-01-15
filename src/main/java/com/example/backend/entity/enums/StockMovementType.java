package com.example.backend.entity.enums;

public enum StockMovementType {
    IN,         // Nhập kho (Mua hàng)
    OUT,        // Xuất kho (Bán hàng)
    RETURN,     // Trả hàng (Khách trả hoặc trả nhà cung cấp)
    ADJUSTMENT  // Điều chỉnh (Kiểm kê kho thấy lệch)
}