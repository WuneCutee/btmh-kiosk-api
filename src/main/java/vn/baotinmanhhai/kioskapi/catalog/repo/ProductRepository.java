package vn.baotinmanhhai.kioskapi.catalog.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.baotinmanhhai.kioskapi.catalog.entity.ProductEntity;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, String> {

    Optional<ProductEntity> findByIdAndEnabledTrueAndPriceIsNotNull(String id);

    @Query("""
            SELECT p FROM ProductEntity p JOIN CategoryEntity c ON c.id = p.categoryId
            WHERE p.enabled = true AND c.enabled = true AND p.price IS NOT NULL
            ORDER BY c.displayOrder ASC, p.quantity ASC, p.createdAt DESC
            """)
    Page<ProductEntity> findAllAcrossCategories(Pageable pageable);

    @Query("""
            SELECT p FROM ProductEntity p
            WHERE p.enabled = true AND p.categoryId = :categoryId AND p.price IS NOT NULL
            ORDER BY p.quantity ASC, p.createdAt DESC
            """)
    Page<ProductEntity> findByCategory(@Param("categoryId") String categoryId, Pageable pageable);

    // Khong con dung LOWER(p.name) LIKE ... o SQL cho tim kiem (II.2.7) - chi so sanh hoa/thuong,
    // khong bo dau ("kim gia bao" se khong khop "Kim Gia Bảo" nhu tai lieu yeu cau). Thay bang 2
    // truy van KHONG loc ten ben duoi, loc + phan trang bang VietnameseText.fold() o ProductService
    // (xem comment o do vi sao khong dung Postgres unaccent extension).
    @Query("""
            SELECT p FROM ProductEntity p JOIN CategoryEntity c ON c.id = p.categoryId
            WHERE p.enabled = true AND c.enabled = true AND p.price IS NOT NULL
            ORDER BY c.displayOrder ASC, p.quantity ASC, p.createdAt DESC
            """)
    List<ProductEntity> findAllEnabledPricedAcrossCategories();

    @Query("""
            SELECT p FROM ProductEntity p
            WHERE p.enabled = true AND p.categoryId = :categoryId AND p.price IS NOT NULL
            ORDER BY p.quantity ASC, p.createdAt DESC
            """)
    List<ProductEntity> findAllEnabledPricedByCategory(@Param("categoryId") String categoryId);

    @Query("""
            SELECT p FROM ProductEntity p JOIN CategoryEntity c ON c.id = p.categoryId
            WHERE p.enabled = true AND c.enabled = true AND p.price IS NOT NULL
            ORDER BY c.displayOrder ASC, p.quantity ASC, p.createdAt DESC
            """)
    List<ProductEntity> findAllForPriceSnapshot();
}
