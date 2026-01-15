package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

  @Query("""
          SELECT p FROM Product p
          WHERE p.name LIKE %:keyword%
             OR p.slug LIKE %:keyword%
      """)
  Page<Product> search(@Param("keyword") String keyword, Pageable pageable);

  @Query("""
          SELECT p FROM Product p
          WHERE (:categoryIds IS NULL OR p.category.id IN :categoryIds)
            AND (:status IS NULL OR p.status = :status)
            AND (:minPrice IS NULL OR p.salePrice >= :minPrice)
            AND (:maxPrice IS NULL OR p.salePrice <= :maxPrice)
            AND (:keyword IS NULL OR p.name LIKE %:keyword% OR p.slug LIKE %:keyword%)
            AND (:hasPromotion IS NULL
                 OR (:hasPromotion = true AND p.discountPrice > 0 AND p.discountPrice < p.salePrice)
                 OR (:hasPromotion = false AND (p.discountPrice IS NULL OR p.discountPrice = 0 OR p.discountPrice >= p.salePrice)))
      """)
  Page<Product> filter(
      @Param("categoryIds") List<Integer> categoryIds,
      @Param("status") Integer status,
      @Param("minPrice") java.math.BigDecimal minPrice,
      @Param("maxPrice") java.math.BigDecimal maxPrice,
      @Param("keyword") String keyword,
      @Param("hasPromotion") Boolean hasPromotion,
      Pageable pageable);

  // ✅ Đếm số sản phẩm theo category (để validate khi xóa)
  long countByCategoryId(Integer categoryId);

  // ✅ Check slug tồn tại
  boolean existsBySlug(String slug);

  boolean existsBySlugAndIdNot(String slug, Integer id);

  boolean existsByName(String name);

  boolean existsByNameAndIdNot(String name, Integer id);

  // 6 sản phẩm mới nhất
  @Query("""
          SELECT p FROM Product p
          WHERE p.createdAt IS not NULL
              AND p.status = 1 and p.qty > 0
          ORDER BY p.createdAt DESC
      """)

  List<Product> findLatestProducts(Pageable pageable);

  // Tìm sản phẩm theo slug
  Optional<Product> findBySlug(String slug);

  // 4 sản phẩm liên quan (cùng category, loại trừ sản phẩm hiện tại)
  @Query("""
          SELECT p FROM Product p
          WHERE p.category.id = :categoryId
            AND p.id <> :productId
            AND p.status = 1
          ORDER BY p.id DESC
      """)
  List<Product> findRelatedProducts(
      @Param("categoryId") Integer categoryId,
      @Param("productId") Integer productId,
      Pageable pageable);

  // Flash Sale: Lấy sản phẩm có giảm giá, ưu tiên mới nhất
  @Query("""
          SELECT p FROM Product p
          WHERE p.status = 1
            AND p.qty > 0
            AND p.discountPrice IS NOT NULL
            AND p.discountPrice < p.salePrice
          ORDER BY p.createdAt DESC
      """)
  List<Product> findFlashSaleProducts(Pageable pageable);

  // Mega Sale: Lấy sản phẩm có giảm giá, ưu tiên giảm giá sâu nhất (%)
  @Query("""
          SELECT p FROM Product p
          WHERE p.status = 1
            AND p.qty > 0
            AND p.discountPrice IS NOT NULL
            AND p.discountPrice < p.salePrice
          ORDER BY (1.0 - CAST(p.discountPrice AS double) / CAST(p.salePrice AS double)) DESC
      """)
  List<Product> findMegaSaleProducts(Pageable pageable);

  // Best Selling (Gợi ý sản phẩm): Lấy sản phẩm bán chạy nhất dựa trên số lượng
  // trong OrderDetail
  @Query("""
          SELECT p FROM Product p
          JOIN OrderDetail od ON od.product.id = p.id
          WHERE p.status = 1
          GROUP BY p.id
          ORDER BY SUM(od.quantity) DESC
      """)
  List<Product> findBestSellingProducts(Pageable pageable);

}
