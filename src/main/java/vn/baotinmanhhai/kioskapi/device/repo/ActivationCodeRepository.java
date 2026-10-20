package vn.baotinmanhhai.kioskapi.device.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.baotinmanhhai.kioskapi.device.entity.ActivationCodeEntity;

import java.util.List;
import java.util.Optional;

public interface ActivationCodeRepository extends JpaRepository<ActivationCodeEntity, String> {

    Optional<ActivationCodeEntity> findFirstByDeviceCodeAndUsedFalseAndRevokedFalseOrderByCreatedAtDesc(String deviceCode);

    List<ActivationCodeEntity> findByDeviceCodeAndUsedFalseAndRevokedFalse(String deviceCode);

    @Modifying
    @Query("DELETE FROM ActivationCodeEntity a WHERE a.deviceCode = :deviceCode")
    int deleteByDeviceCode(@Param("deviceCode") String deviceCode);
}
