package vn.baotinmanhhai.kioskapi.catalog.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.baotinmanhhai.kioskapi.catalog.entity.AdEntity;

import java.util.List;

public interface AdRepository extends JpaRepository<AdEntity, String> {
    List<AdEntity> findByEnabledTrueOrderByOrderAsc();
}
