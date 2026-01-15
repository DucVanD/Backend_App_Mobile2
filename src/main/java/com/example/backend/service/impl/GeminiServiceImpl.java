package com.example.backend.service.impl;

import com.example.backend.config.GeminiConfig;
import com.example.backend.dto.ChatMessageDto;
import com.example.backend.dto.ChatResponse;
import com.example.backend.dto.ProductDto;
import com.example.backend.entity.Order;
import com.example.backend.entity.Product;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.ProductRepository;
import com.example.backend.service.CartService;
import com.example.backend.service.AiChatService;
import com.example.backend.service.AiMemoryService;
import com.example.backend.service.AiReasoningService;
import com.example.backend.dto.AiDecision;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeminiServiceImpl implements AiChatService {

    @Autowired
    private GeminiConfig geminiConfig;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private AiMemoryService aiMemoryService;

    @Autowired
    private AiReasoningService aiReasoningService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChatResponse chat(String message, Integer userId, List<ChatMessageDto> history) throws Exception {
        // Build request with function declarations and history
        Map<String, Object> request = buildGeminiRequest(message, history);

        // Call Gemini API
        String url = geminiConfig.getApiUrl() + "?key=" + geminiConfig.getApiKey();

        System.out.println(">>> Calling Gemini API: " + geminiConfig.getModel());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        int maxRetries = 5;
        int retryCount = 0;
        long waitTime = 3000; // 3 seconds initial wait

        while (true) {
            try {
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
                // Use HttpMethod.POST directly to satisfy @NonNull if possible, or valueOf
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

                System.out.println(">>> Gemini Response Status: " + response.getStatusCode());

                return parseGeminiResponse(response.getBody(), userId);
            } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
                retryCount++;
                if (retryCount > maxRetries) {
                    System.err.println(">>> Gemini API Error: Quota exceeded after " + maxRetries + " retries.");
                    throw new Exception(
                            "Hệ thống AI hiện đang bận do quá nhiều người sử dụng cùng lúc. Bạn vui lòng đợi khoảng 1 phút rồi thử lại nhé!");
                }

                System.out.println(
                        ">>> Gemini Rate Limit (429). Retrying in " + waitTime + "ms... (Attempt " + retryCount + ")");
                Thread.sleep(waitTime);
                waitTime *= 2;
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                System.err.println(">>> Gemini API Error: " + e.getResponseBodyAsString());
                throw e;
            } catch (Exception e) {
                System.err.println(">>> System Error: " + e.getMessage());
                throw e;
            }
        }
    }

    private Map<String, Object> buildGeminiRequest(String message, List<ChatMessageDto> history) {
        Map<String, Object> request = new HashMap<>();

        // System instruction
        String systemInstruction = "Bạn là trợ lý AI cho cửa hàng thời trang. " +
                "Nhiệm vụ: tìm sản phẩm, thêm vào giỏ, kiểm tra đơn hàng, tư vấn mua sắm. " +
                "Trả lời bằng tiếng Việt, thân thiện. " +
                "Dùng function searchProducts để tìm sản phẩm thực. \n\n" +

                "QUY TRÌNH RA QUYẾT ĐỊNH (REASONING): \n" +
                "- Hệ thống có tầng logic kiểm tra tồn kho và ghi nhớ ngữ cảnh (Contextual Memory).\n" +
                "- Khi user muốn thêm vào giỏ hàng ('cái đầu', 'cái này', 'thêm nó'):\n" +
                "  1. GỌI addToCart ngay. Nếu user không nói ID, hãy để productId là 0.\n" +
                "  2. Tầng Reasoning sẽ tự động phân tích: 'User đang nói đến cái nào?' -> 'Còn hàng không?' -> 'Quyết định'.\n"
                +
                "- Bạn đóng vai trò người giao tiếp, hãy dựa vào kết quả từ function để giải thích cho khách hàng.\n" +
                "- Tránh nhắc đến các thuật ngữ kỹ thuật như 'ID', 'Function' với khách hàng.";

        request.put("system_instruction", Map.of("parts", List.of(Map.of("text", systemInstruction))));

        // Conversation History + User Message
        List<Map<String, Object>> contents = new ArrayList<>();

        if (history != null) {
            for (ChatMessageDto msg : history) {
                Map<String, Object> content = new HashMap<>();
                content.put("role", msg.getRole());
                content.put("parts", List.of(Map.of("text", msg.getContent())));
                contents.add(content);
            }
        }

        // Current User message
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("parts", List.of(Map.of("text", message)));
        contents.add(userMessage);

        request.put("contents", contents);

        // Function declarations
        request.put("tools", List.of(Map.of("function_declarations", getFunctionDeclarations())));

        return request;
    }

    private List<Map<String, Object>> getFunctionDeclarations() {
        List<Map<String, Object>> functions = new ArrayList<>();

        // Function 1: Search Products
        Map<String, Object> searchProducts = new HashMap<>();
        searchProducts.put("name", "searchProducts");
        searchProducts.put("description", "Tìm kiếm sản phẩm theo từ khóa, danh mục, khoảng giá");

        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put("type", "object");
        Map<String, Object> properties = new HashMap<>();
        properties.put("query", Map.of("type", "string", "description", "Từ khóa tìm kiếm (tên sản phẩm)"));
        properties.put("minPrice", Map.of("type", "number", "description", "Giá tối thiểu"));
        properties.put("maxPrice", Map.of("type", "number", "description", "Giá tối đa"));
        properties.put("isSale", Map.of("type", "boolean", "description", "Chỉ tìm sản phẩm đang giảm giá"));
        searchParams.put("properties", properties);
        searchProducts.put("parameters", searchParams);
        functions.add(searchProducts);

        // Function 2: Get Product Details
        Map<String, Object> getProduct = new HashMap<>();
        getProduct.put("name", "getProductDetails");
        getProduct.put("description", "Lấy thông tin chi tiết của một sản phẩm");
        Map<String, Object> productParams = new HashMap<>();
        productParams.put("type", "object");
        productParams.put("properties",
                Map.of("productId", Map.of("type", "integer", "description", "ID của sản phẩm")));
        productParams.put("required", List.of("productId"));
        getProduct.put("parameters", productParams);
        functions.add(getProduct);

        // Function 3: Get Order Status
        Map<String, Object> getOrder = new HashMap<>();
        getOrder.put("name", "getOrderStatus");
        getOrder.put("description", "Kiểm tra trạng thái đơn hàng");
        Map<String, Object> orderParams = new HashMap<>();
        orderParams.put("type", "object");
        orderParams.put("properties", Map.of("orderId", Map.of("type", "integer", "description", "Mã đơn hàng")));
        orderParams.put("required", List.of("orderId"));
        getOrder.put("parameters", orderParams);
        // Function 4: Add to Cart
        Map<String, Object> addToCart = new HashMap<>();
        addToCart.put("name", "addToCart");
        addToCart.put("description",
                "GỌI FUNCTION NÀY khi user muốn thêm sản phẩm vào giỏ hàng. " +
                        "Hỗ trợ các cụm từ: 'thêm vào giỏ', 'lấy cái này', 'mua cái đầu tiên'. " +
                        "Nếu user không chỉ định ID cụ thể, hãy để productId là 0. " +
                        "Hệ thống sẽ tự truy xuất từ bộ nhớ sản phẩm vừa xem.");
        Map<String, Object> cartParams = new HashMap<>();
        cartParams.put("type", "object");
        Map<String, Object> cartProps = new HashMap<>();
        cartProps.put("productId", Map.of("type", "integer", "description", "ID của sản phẩm cần thêm"));
        cartProps.put("quantity", Map.of("type", "integer", "description", "Số lượng (mặc định là 1)"));
        cartParams.put("properties", cartProps);
        cartParams.put("required", List.of("productId"));
        addToCart.put("parameters", cartParams);
        functions.add(addToCart);

        return functions;
    }

    private ChatResponse parseGeminiResponse(String responseBody, Integer userId) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode candidates = root.get("candidates");

        if (candidates == null || candidates.isEmpty()) {
            return new ChatResponse("Xin lỗi, tôi không thể trả lời câu hỏi này.");
        }

        JsonNode firstCandidate = candidates.get(0);
        JsonNode content = firstCandidate.get("content");
        JsonNode parts = content.get("parts");

        if (parts == null || parts.isEmpty()) {
            return new ChatResponse("Xin lỗi, tôi không thể trả lời câu hỏi này.");
        }

        JsonNode firstPart = parts.get(0);

        // Check if it's a function call
        if (firstPart.has("functionCall")) {
            return handleFunctionCall(firstPart.get("functionCall"), userId);
        }

        // Regular text response
        String text = firstPart.get("text").asText();
        return new ChatResponse(text);
    }

    private ChatResponse handleFunctionCall(JsonNode functionCall, Integer userId) throws Exception {
        String functionName = functionCall.get("name").asText();
        JsonNode args = functionCall.get("args");

        switch (functionName) {
            case "searchProducts":
                return handleSearchProducts(args, userId);
            case "getProductDetails":
                return handleGetProductDetails(args);
            case "getOrderStatus":
                return handleGetOrderStatus(args, userId);
            case "addToCart":
                return handleAddToCart(args, userId);
            default:
                return new ChatResponse("Xin lỗi, tôi không thể thực hiện yêu cầu này.");
        }
    }

    private ChatResponse handleSearchProducts(JsonNode args, Integer userId) {
        String query = args.has("query") ? args.get("query").asText() : "";
        BigDecimal minPrice = args.has("minPrice") ? BigDecimal.valueOf(args.get("minPrice").asDouble()) : null;
        BigDecimal maxPrice = args.has("maxPrice") ? BigDecimal.valueOf(args.get("maxPrice").asDouble()) : null;
        boolean isSale = args.has("isSale") && args.get("isSale").asBoolean();

        List<Product> products = productRepository.findAll().stream()
                .filter(p -> query.isEmpty() || p.getName().toLowerCase().contains(query.toLowerCase()))
                .filter(p -> minPrice == null || p.getSalePrice().compareTo(minPrice) >= 0)
                .filter(p -> maxPrice == null || p.getSalePrice().compareTo(maxPrice) <= 0)
                .filter(p -> !isSale || p.getDiscountPrice() != null)
                .limit(5)
                .collect(Collectors.toList());

        // Save found product IDs to memory for reasoning layer
        if (userId != null) {
            List<Integer> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
            aiMemoryService.setLastSuggestedProducts(userId, productIds);
        }

        if (products.isEmpty()) {
            return new ChatResponse("Không tìm thấy sản phẩm phù hợp. Bạn có thể thử tìm kiếm với từ khóa khác.");
        }

        List<ProductDto> productDtos = products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // Build message with explicit IDs for AI to remember
        StringBuilder message = new StringBuilder(
                String.format("Tôi tìm thấy %d sản phẩm phù hợp:\n\n", products.size()));
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            message.append(String.format("%d. **%s** (ID: %d) - Giá: %s\n",
                    i + 1, p.getName(), p.getId(), formatPrice(p.getSalePrice())));
        }
        message.append("\nBạn có thể nói 'thêm cái đầu vào giỏ' hoặc 'thêm 2 cái đầu' để tôi thêm sản phẩm cho bạn!");

        return new ChatResponse(message.toString(), productDtos);
    }

    private String formatPrice(BigDecimal price) {
        return String.format("%,d đ", price.longValue());
    }

    private ChatResponse handleGetProductDetails(JsonNode args) {
        int productId = args.get("productId").asInt();
        Optional<Product> productOpt = productRepository.findById(productId);

        if (productOpt.isEmpty()) {
            return new ChatResponse("Không tìm thấy sản phẩm với ID: " + productId);
        }

        Product product = productOpt.get();
        String message = String.format(
                "Thông tin sản phẩm:\n" +
                        "Tên: %s\n" +
                        "Giá: %,.0f₫\n" +
                        "Tồn kho: %d\n" +
                        "Mô tả: %s",
                product.getName(),
                product.getSalePrice(),
                product.getQty(),
                product.getDetail() != null ? product.getDetail() : "Chưa có mô tả");

        return new ChatResponse(message, List.of(convertToDto(product)));
    }

    private ChatResponse handleGetOrderStatus(JsonNode args, Integer userId) {
        int orderId = args.get("orderId").asInt();
        Optional<Order> orderOpt = orderRepository.findById(orderId);

        if (orderOpt.isEmpty()) {
            return new ChatResponse("Không tìm thấy đơn hàng với mã: " + orderId);
        }

        Order order = orderOpt.get();

        // Security: Check if order belongs to user
        if (userId != null && !order.getUser().getId().equals(userId)) {
            return new ChatResponse("Bạn không có quyền xem đơn hàng này.");
        }

        String statusText = getOrderStatusText(order.getStatus().toString());
        String paymentStatusText = getPaymentStatusText(order.getPaymentStatus().toString());

        String message = String.format(
                "Thông tin đơn hàng #%d:\n" +
                        "Trạng thái: %s\n" +
                        "Thanh toán: %s\n" +
                        "Tổng tiền: %,.0f₫\n" +
                        "Ngày đặt: %s",
                order.getId(),
                statusText,
                paymentStatusText,
                order.getTotalAmount(),
                order.getCreatedAt());

        return new ChatResponse(message);
    }

    private ChatResponse handleAddToCart(JsonNode args, Integer userId) {
        if (userId == null) {
            return new ChatResponse("Bạn cần đăng nhập để thêm sản phẩm vào giỏ hàng.");
        }

        int productId = args.get("productId").asInt();
        int quantity = args.has("quantity") ? args.get("quantity").asInt() : 1;

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return new ChatResponse("Rất tiếc, tôi không tìm thấy sản phẩm có ID: " + productId);
        }

        Product product = productOpt.get(); // We might not need this here anymore but keeping for reference if needed

        // Use Reasoning Layer to decide
        AiDecision decision = aiReasoningService.decideAddToCart(userId, productId, quantity);

        if (!decision.approved()) {
            return new ChatResponse(decision.reasoning());
        }

        try {
            cartService.addItem(userId, decision.resolvedProductId(), decision.resolvedQuantity());

            // Get product again if ID was resolved from memory
            Product resolvedProduct = productRepository.findById(decision.resolvedProductId()).orElse(product);

            return new ChatResponse(String.format("Đã thêm thành công %d sản phẩm '%s' vào giỏ hàng của bạn! 🎉",
                    decision.resolvedQuantity(), resolvedProduct.getName()));
        } catch (Exception e) {
            return new ChatResponse("Có lỗi xảy ra khi thêm sản phẩm vào giỏ hàng: " + e.getMessage());
        }
    }

    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSalePrice(product.getSalePrice());
        dto.setDiscountPrice(product.getDiscountPrice());
        dto.setImage(product.getImage());
        return dto;
    }

    private String getOrderStatusText(String status) {
        switch (status) {
            case "PENDING":
                return "Đang chờ xác nhận";
            case "CONFIRMED":
                return "Đã xác nhận";
            case "SHIPPING":
                return "Đang giao hàng";
            case "COMPLETED":
                return "Đã giao";
            case "CANCELLED":
                return "Đã hủy";
            default:
                return status;
        }
    }

    private String getPaymentStatusText(String status) {
        switch (status) {
            case "UNPAID":
                return "Chưa thanh toán";
            case "PAID":
                return "Đã thanh toán";
            case "FAILED":
                return "Thanh toán thất bại";
            case "REFUNDED":
                return "Đã hoàn tiền";
            default:
                return status;
        }
    }
}
