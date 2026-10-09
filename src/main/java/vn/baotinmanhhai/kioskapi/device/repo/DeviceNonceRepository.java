package vn.baotinmanhhai.kioskapi.device.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.baotinmanhhai.kioskapi.device.entity.DeviceNonceEntity;

import java.time.Instant;

public interface DeviceNonceRepository extends JpaRepository<DeviceNonceEntity, DeviceNonceEntity.Key> {

    boolean existsByIdDeviceCodeAndIdNonceAndCreatedAtAfter(String deviceCode, String nonce, Instant after);

    @Modifying
    @Query("DELETE FROM DeviceNonceEntity n WHERE n.createdAt < :cutoff")
    int deleteOlderThan(@Param("cutoff") Instant cutoff);
}
