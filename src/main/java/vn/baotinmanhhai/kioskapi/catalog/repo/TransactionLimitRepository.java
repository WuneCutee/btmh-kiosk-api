package vn.baotinmanhhai.kioskapi.catalog.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.baotinmanhhai.kioskapi.catalog.entity.TransactionLimitEntity;

public interface TransactionLimitRepository extends JpaRepository<TransactionLimitEntity, Long> {
}
