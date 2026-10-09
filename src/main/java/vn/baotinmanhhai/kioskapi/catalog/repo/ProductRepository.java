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

    @Query("""
            SELECT p FROM ProductEntity p JOIN CategoryEntity c ON c.id = p.categoryId
            WHERE p.enabled = true AND c.enabled = true AND p.price IS NOT NULL
            AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))
            ORDER BY c.displayOrder ASC, p.quantity ASC, p.createdAt DESC
            """)
    Page<ProductEntity> searchAllCategories(@Param("name") String name, Pageable pageable);

    @Query("""
            SELECT p FROM ProductEntity p
            WHERE p.enabled = true AND p.categoryId = :categoryId AND p.price IS NOT NULL
            AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))
            ORDER BY p.quantity ASC, p.createdAt DESC
            """)
    Page<ProductEntity> searchByCategory(@Param("categoryId") String categoryId, @Param("name") String name, Pageable pageable);

    @Query("""
            SELECT p FROM ProductEntity p JOIN CategoryEntity c ON c.id = p.categoryId
            WHERE p.enabled = true AND c.enabled = true AND p.price IS NOT NULL
            ORDER BY c.displayOrder ASC, p.quantity ASC, p.createdAt DESC
            """)
    List<ProductEntity> findAllForPriceSnapshot();
}
