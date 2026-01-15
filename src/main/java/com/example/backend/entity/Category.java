package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Dùng Integer theo yêu cầu

    // Map parent_id (Đa cấp)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id") // Tự động là nullable = true mặc định
    private Category parent;

    // (Tùy chọn) Để lấy danh sách con nếu cần
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    private String image;
    private String imagePublicId;

    @Column(columnDefinition = "TEXT") // Description thường dài
    private String description;

    @Builder.Default
    private Integer status = 1; // 1: Active, 0: Hidden

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Virtual Column: Đếm số sản phẩm (yêu cầu Hibernate)
    @org.hibernate.annotations.Formula("(SELECT count(*) FROM products p WHERE p.category_id = id)")
    private Long productCount;
}