package com.example.backend.service.impl;

import com.example.backend.config.CloudinaryService;
import com.example.backend.dto.ProductDto;
import com.example.backend.dto.CategoryHomeDto;
import com.example.backend.entity.Category;
import com.example.backend.entity.Product;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.mapper.ProductMapper;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.ProductRepository;
import com.example.backend.service.ProductService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;

    // =====================
    // GET ALL
    // =====================
    @Override
    public List<ProductDto> getAll() {
        return productRepository.findAll()
                .stream()
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductDto> getPage(int page, int size) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by("id").descending());
        return productRepository.findAll(pageable)
                .map(ProductMapper::toDto);
    }

    // =====================
    // SEARCH
    // =====================
    @Override
    public List<ProductDto> search(String keyword) {
        return productRepository.search(keyword, Pageable.unpaged())
                .getContent()
                .stream()
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductDto> search(String keyword, int page, int size) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by("id").descending());
        return productRepository.search(keyword, pageable)
                .map(ProductMapper::toDto);
    }

    // =====================
    // FILTER
    // =====================
    @Override
    public List<ProductDto> filter(List<Integer> categoryIds, Integer status,
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice, String keyword, Boolean hasPromotion) {
        return productRepository
                .filter(categoryIds, status, minPrice, maxPrice, keyword, hasPromotion, Pageable.unpaged())
                .getContent()
                .stream()
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductDto> filter(List<Integer> categoryIds, Integer status,
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice, String keyword, Boolean hasPromotion, String sortBy, int page, int size) {

        org.springframework.data.domain.Sort sort;
        if ("price_asc".equals(sortBy)) {
            sort = org.springframework.data.domain.Sort.by("salePrice").ascending();
        } else if ("price_desc".equals(sortBy)) {
            sort = org.springframework.data.domain.Sort.by("salePrice").descending();
        } else {
            sort = org.springframework.data.domain.Sort.by("id").descending();
        }

        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return productRepository.filter(categoryIds, status, minPrice, maxPrice, keyword, hasPromotion, pageable)
                .map(ProductMapper::toDto);
    }

    // =====================
    // GET BY ID
    // =====================
    @Override
    public ProductDto getById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return ProductMapper.toDto(product);
    }

    @Override
    public ProductDto getBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product with slug '" + slug + "' not found"));
        return ProductMapper.toDto(product);
    }

    // =====================
    // CREATE
    // =====================
    @Override
    public ProductDto create(ProductDto dto) {
        // 1. Validate Price (Discount <= Sale)
        if (dto.getDiscountPrice() != null && dto.getSalePrice().compareTo(dto.getDiscountPrice()) < 0) {
            throw new IllegalArgumentException("discountPrice:Giá khuyến mãi không được lớn hơn giá bán");
        }

        // 2. Validate Duplicate Name
        if (productRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("name:Tên sản phẩm đã tồn tại");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = ProductMapper.toEntity(dto, category);

        // 3. Auto-generate slug
        if (product.getSlug() == null || product.getSlug().isBlank()) {
            String baseSlug = generateSlugFromName(dto.getName());
            product.setSlug(generateUniqueSlug(baseSlug, null));
        } else {
            product.setSlug(generateUniqueSlug(dto.getSlug(), null));
        }

        Product saved = productRepository.save(product);
        return ProductMapper.toDto(saved);
    }

    // =====================
    // UPDATE
    // =====================
    @Override
    public ProductDto update(Integer id, ProductDto dto) {
        // 1. Validate Price (Discount <= Sale)
        if (dto.getDiscountPrice() != null && dto.getSalePrice().compareTo(dto.getDiscountPrice()) < 0) {
            throw new IllegalArgumentException("discountPrice:Giá khuyến mãi không được lớn hơn giá bán");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 2. Validate Duplicate Name
        if (productRepository.existsByNameAndIdNot(dto.getName(), id)) {
            throw new IllegalArgumentException("name:Tên sản phẩm đã tồn tại");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // 3. Slug Update Logic
        boolean nameChanged = !product.getName().equals(dto.getName());
        boolean slugIsOldAuto = java.util.Objects.equals(product.getSlug(), dto.getSlug())
                || dto.getSlug() == null
                || dto.getSlug().isBlank();

        product.setName(dto.getName());

        if (nameChanged && slugIsOldAuto) {
            String baseSlug = generateSlugFromName(dto.getName());
            product.setSlug(generateUniqueSlug(baseSlug, id));
        } else if (dto.getSlug() != null && !dto.getSlug().isBlank() && !dto.getSlug().equals(product.getSlug())) {
            product.setSlug(generateUniqueSlug(dto.getSlug(), id));
        }

        product.setDescription(dto.getDescription());
        product.setDetail(dto.getDetail());
        product.setSalePrice(dto.getSalePrice());
        product.setDiscountPrice(dto.getDiscountPrice());
        product.setQty(dto.getQty());
        product.setStatus(dto.getStatus());
        product.setCategory(category);

        // ✅ CHỈ SET ẢNH KHI CÓ ẢNH MỚI
        if (dto.getImage() != null && !dto.getImage().isBlank()) {
            if (product.getImagePublicId() != null && !product.getImagePublicId().equals(dto.getImagePublicId())) {
                try {
                    cloudinaryService.deleteImage(product.getImagePublicId());
                } catch (Exception e) {
                    System.err.println("Lỗi xóa ảnh cũ khi update product: " + e.getMessage());
                }
            }
            product.setImage(dto.getImage());
            product.setImagePublicId(dto.getImagePublicId());
        }

        Product updated = productRepository.save(product);
        return ProductMapper.toDto(updated);
    }

    // =====================
    // DELETE (XÓA ẢNH + DB)
    // =====================
    @Override
    @Transactional
    public void delete(Integer id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 1️⃣ XÓA ẢNH CLOUDINARY
        try {
            cloudinaryService.deleteImage(product.getImagePublicId());
        } catch (Exception e) {
            System.err.println("Không xóa được ảnh Cloudinary: " + e.getMessage());
        }

        // 2️⃣ XÓA DB
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public void toggleStatus(Integer id) {
        if (id == null)
            return;
        productRepository.findById(id).ifPresent(product -> {
            Integer status = product.getStatus();
            if (status != null) {
                product.setStatus(status == 1 ? 0 : 1);
                productRepository.save(product);
            }
        });
    }

    private String generateSlugFromName(String name) {
        if (name == null || name.isBlank()) {
            return "product";
        }
        return java.text.Normalizer.normalize(name, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("đ", "d")
                .replaceAll("Đ", "d")
                .toLowerCase()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("-+", "-")
                .replaceAll("^-+|-+$", "");
    }

    private String generateUniqueSlug(String baseSlug, Integer excludeId) {
        String slug = baseSlug;
        int counter = 1;

        while (true) {
            boolean exists = (excludeId != null)
                    ? productRepository.existsBySlugAndIdNot(slug, excludeId)
                    : productRepository.existsBySlug(slug);

            if (!exists) {
                return slug;
            }

            slug = baseSlug + "-" + counter;
            counter++;
        }
    }

    @Override
    public List<CategoryHomeDto> getCategoriesHome() {
        return categoryRepository.findByParentIsNull().stream()
                .map(cat -> {
                    // Fetch top 4 products for each parent category
                    Pageable topFour = PageRequest.of(0, 4, org.springframework.data.domain.Sort.by("id").descending());
                    List<ProductDto> products = productRepository.filter(
                            List.of(cat.getId()), 1, null, null, null, null, topFour)
                            .getContent()
                            .stream()
                            .map(ProductMapper::toDto)
                            .toList();

                    return CategoryHomeDto.builder()
                            .id(cat.getId())
                            .name(cat.getName())
                            .slug(cat.getSlug())
                            .products(products)
                            .build();
                })
                .filter(dto -> !dto.getProducts().isEmpty())
                .limit(3)
                .toList();
    }

    @Override
    public List<ProductDto> getLatestProducts(int limit) {
        return productRepository.findLatestProducts(PageRequest.of(0, limit)).stream()
                .map(ProductMapper::toDto)
                .toList();
    }

    @Override
    public List<ProductDto> getRelatedProducts(Integer categoryId, Integer productId, int limit) {
        return productRepository.findRelatedProducts(categoryId, productId, PageRequest.of(0, limit))
                .stream()
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> getFlashSaleProducts(int limit) {
        return productRepository.findFlashSaleProducts(PageRequest.of(0, limit)).stream()
                .map(ProductMapper::toDto)
                .toList();
    }

    @Override
    public List<ProductDto> getMegaSaleProducts(int limit) {
        return productRepository.findMegaSaleProducts(PageRequest.of(0, limit)).stream()
                .map(ProductMapper::toDto)
                .toList();
    }

    @Override
    public List<ProductDto> getBestSellingProducts(int limit) {
        return productRepository.findBestSellingProducts(PageRequest.of(0, limit)).stream()
                .map(ProductMapper::toDto)
                .toList();
    }

}
