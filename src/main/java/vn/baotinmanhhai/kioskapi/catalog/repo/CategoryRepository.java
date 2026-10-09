package vn.baotinmanhhai.kioskapi.catalog.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.baotinmanhhai.kioskapi.catalog.entity.CategoryEntity;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, String> {
    List<CategoryEntity> findByEnabledTrueOrderByDisplayOrderAsc();

    Optional<CategoryEntity> findByIdAndEnabledTrue(String id);
}
