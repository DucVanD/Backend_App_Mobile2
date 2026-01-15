package com.example.backend.service.impl;

import com.example.backend.dto.AiDecision;
import com.example.backend.entity.Product;
import com.example.backend.repository.ProductRepository;
import com.example.backend.service.AiMemoryService;
import com.example.backend.service.AiReasoningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AiReasoningServiceImpl implements AiReasoningService {

    @Autowired
    private AiMemoryService aiMemoryService;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public AiDecision decideAddToCart(Integer userId, Integer productId, int quantity) {
        StringBuilder logs = new StringBuilder();
        logs.append("[Reasoning Layer] Phân tích yêu cầu thêm vào giỏ hàng...\n");

        int finalProductId = -1;
        int finalQuantity = quantity > 0 ? quantity : 1;

        // 1. Resolve Product ID
        if (productId != null && productId > 0) {
            finalProductId = productId;
            logs.append(String.format("- User cung cấp Product ID trực tiếp: %d\n", finalProductId));
        } else {
            // Need to resolve from memory
            List<Integer> memory = aiMemoryService.getLastSuggestedProducts(userId);
            if (memory.isEmpty()) {
                logs.append("- LỖI: Không tìm thấy sản phẩm nào trong bộ nhớ gần đây.\n");
                return new AiDecision(false,
                        "Tôi không nhớ bạn đang nói đến sản phẩm nào. Bạn vui lòng tìm kiếm lại nhé!", null, 0);
            }
            // Default to the first one if not specified or ambiguous
            finalProductId = memory.get(0);
            logs.append(String.format("- Memory resolve: Lấy sản phẩm đầu tiên từ danh sách vừa gợi ý (ID: %d)\n",
                    finalProductId));
        }

        // 2. Validate Product Existence and Stock
        Optional<Product> productOpt = productRepository.findById(finalProductId);
        if (productOpt.isEmpty()) {
            logs.append(String.format("- LỖI: Sản phẩm ID %d không tồn tại trong database.\n", finalProductId));
            return new AiDecision(false, "Sản phẩm này hiện không còn tồn tại.", null, 0);
        }

        Product product = productOpt.get();
        logs.append(String.format("- Kiểm tra sản phẩm: '%s' (Tồn kho: %d)\n", product.getName(), product.getQty()));

        if (product.getQty() < finalQuantity) {
            logs.append(String.format("- LỖI: Không đủ hàng (Yêu cầu: %d, Hiện có: %d)\n", finalQuantity,
                    product.getQty()));
            return new AiDecision(false,
                    String.format("Rất tiếc, sản phẩm '%s' chỉ còn %d cái trong kho, không đủ số lượng %d bạn yêu cầu.",
                            product.getName(), product.getQty(), finalQuantity),
                    null, 0);
        }

        logs.append("- QUYẾT ĐỊNH: Chấp nhận yêu cầu.\n");
        System.out.println(logs.toString()); // Log to console for "academic proof"

        return new AiDecision(true, logs.toString(), finalProductId, finalQuantity);
    }
}
