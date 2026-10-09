package vn.baotinmanhhai.kioskapi.catalog.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.baotinmanhhai.kioskapi.catalog.entity.BannerEntity;

import java.util.List;

public interface BannerRepository extends JpaRepository<BannerEntity, String> {
    List<BannerEntity> findByEnabledTrueOrderByPriorityAsc();
}
