package com.example.backend.service.impl;

import com.example.backend.config.CloudinaryService;
import com.example.backend.dto.CategoryDto;
import com.example.backend.entity.Category;
import com.example.backend.mapper.CategoryMapper;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.ProductRepository;
import com.example.backend.service.CategoryService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;
    private final ProductRepository productRepository;

    @Override
    public List<CategoryDto> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryDto getById(Integer id) {
        Objects.requireNonNull(id, "Category ID must not be null");

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return CategoryMapper.toDto(category);
    }

    @Override
    @Transactional
    public CategoryDto create(CategoryDto dto) {
        Objects.requireNonNull(dto, "CategoryDto must not be null");

        if (dto.getSlug() == null || dto.getSlug().isBlank()) {
            String baseSlug = generateSlugFromName(dto.getName());
            dto.setSlug(generateUniqueSlug(baseSlug, null));
        } else {
            dto.setSlug(generateUniqueSlug(dto.getSlug(), null));
        }

        Category category = CategoryMapper.toEntity(dto);

        if (dto.getParentId() != null) {
            Integer parentId = dto.getParentId();
            Category parent = categoryRepository
                    .findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            category.setParent(parent);
        }

        Category saved = categoryRepository.save(category);
        return CategoryMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CategoryDto update(Integer id, CategoryDto dto) {
        Objects.requireNonNull(id, "Category ID must not be null");
        Objects.requireNonNull(dto, "CategoryDto must not be null");

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (dto.getSlug() == null || dto.getSlug().isBlank()) {
            String baseSlug = generateSlugFromName(dto.getName());
            dto.setSlug(generateUniqueSlug(baseSlug, id));
        } else if (!category.getSlug().equals(dto.getSlug())) {
            dto.setSlug(generateUniqueSlug(dto.getSlug(), id));
        }

        category.setName(dto.getName());
        category.setSlug(dto.getSlug());
        category.setDescription(dto.getDescription());
        category.setStatus(dto.getStatus());

        if (dto.getImage() != null && !dto.getImage().isBlank()) {
            if (category.getImagePublicId() != null && !category.getImagePublicId().equals(dto.getImagePublicId())) {
                try {
                    cloudinaryService.deleteImage(category.getImagePublicId());
                } catch (Exception e) {
                    System.err.println("Lỗi xóa ảnh cũ khi update category: " + e.getMessage());
                }
            }
            category.setImage(dto.getImage());
            category.setImagePublicId(dto.getImagePublicId());
        }

        if (dto.getParentId() != null) {
            if (dto.getParentId().equals(id)) {
                throw new IllegalArgumentException("Danh mục không thể là cha của chính nó");
            }
            if (isDescendant(id, dto.getParentId())) {
                throw new IllegalArgumentException("Không thể set danh mục con làm cha (tạo vòng lặp)");
            }
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        Category saved = java.util.Objects.requireNonNull(categoryRepository.save(category));
        return CategoryMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Objects.requireNonNull(id, "Category ID must not be null");

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new IllegalStateException("Không thể xóa danh mục đang có sản phẩm.");
        }

        long childCount = categoryRepository.countByParentId(id);
        if (childCount > 0) {
            throw new IllegalStateException("Không thể xóa danh mục cha đang có danh mục con.");
        }

        try {
            cloudinaryService.deleteImage(category.getImagePublicId());
        } catch (Exception e) {
            System.err.println("Không xóa được ảnh Cloudinary: " + e.getMessage());
        }

        categoryRepository.delete(category);
    }

    @Override
    @Transactional
    public List<CategoryDto> bulkCreate(List<CategoryDto> dtos) {
        List<Category> categories = dtos.stream()
                .map(dto -> {
                    Category category = CategoryMapper.toEntity(dto);
                    if (dto.getParentId() != null) {
                        categoryRepository.findById(dto.getParentId()).ifPresent(category::setParent);
                    }
                    return category;
                })
                .toList();

        return categoryRepository.saveAll(categories)
                .stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    @Override
    public Page<CategoryDto> getPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return categoryRepository.findAll(pageable).map(CategoryMapper::toDto);
    }

    @Override
    @Transactional
    public void toggleStatus(Integer id) {
        if (id == null)
            return;
        categoryRepository.findById(id).ifPresent(category -> {
            Integer status = category.getStatus();
            if (status != null) {
                category.setStatus(status == 1 ? 0 : 1);
                categoryRepository.save(category);
            }
        });
    }

    @Override
    public List<CategoryDto> getParentsWithChildren() {
        return categoryRepository.findByParentIsNull()
                .stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    private String generateSlugFromName(String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFD)
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
                    ? categoryRepository.existsBySlugAndIdNot(slug, excludeId)
                    : categoryRepository.existsBySlug(slug);
            if (!exists)
                return slug;
            slug = baseSlug + "-" + counter;
            counter++;
        }
    }

    private boolean isDescendant(Integer ancestorId, Integer descendantId) {
        if (ancestorId == null || descendantId == null)
            return false;

        Category current = categoryRepository.findById(descendantId).orElse(null);
        while (current != null) {
            Category parent = current.getParent();
            if (parent != null) {
                if (parent.getId().equals(ancestorId)) {
                    return true;
                }
                current = parent;
            } else {
                break;
            }
        }
        return false;
    }
}