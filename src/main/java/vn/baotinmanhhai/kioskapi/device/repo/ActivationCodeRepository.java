package vn.baotinmanhhai.kioskapi.device.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.baotinmanhhai.kioskapi.device.entity.ActivationCodeEntity;

import java.util.Optional;

public interface ActivationCodeRepository extends JpaRepository<ActivationCodeEntity, String> {

    Optional<ActivationCodeEntity> findFirstByDeviceCodeAndUsedFalseAndRevokedFalseOrderByCreatedAtDesc(String deviceCode);
}
