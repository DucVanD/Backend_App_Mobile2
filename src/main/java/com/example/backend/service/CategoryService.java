package com.example.backend.service;

import com.example.backend.dto.CategoryDto;
import java.util.List;

import org.springframework.data.domain.Page;

public interface CategoryService {

    List<CategoryDto> getAll();

    CategoryDto getById(Integer id);

    CategoryDto create(CategoryDto dto);

    CategoryDto update(Integer id, CategoryDto dto);

    void delete(Integer id);

    List<CategoryDto> bulkCreate(List<CategoryDto> dtos);

    Page<CategoryDto> getPage(int page, int size);

    void toggleStatus(Integer id);

    List<CategoryDto> getParentsWithChildren();
}
