package vn.baotinmanhhai.kioskapi.catalog.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.baotinmanhhai.kioskapi.catalog.entity.ProvinceEntity;

import java.util.List;

public interface ProvinceRepository extends JpaRepository<ProvinceEntity, Long> {
    List<ProvinceEntity> findByEnabledTrue();
}
