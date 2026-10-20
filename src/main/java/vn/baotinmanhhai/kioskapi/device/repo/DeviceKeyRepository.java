package vn.baotinmanhhai.kioskapi.device.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.baotinmanhhai.kioskapi.device.entity.DeviceKeyEntity;

import java.util.List;
import java.util.Optional;

public interface DeviceKeyRepository extends JpaRepository<DeviceKeyEntity, String> {

    Optional<DeviceKeyEntity> findByDeviceCodeAndStatus(String deviceCode, String status);

    List<DeviceKeyEntity> findByDeviceCode(String deviceCode);

    List<DeviceKeyEntity> findByDeviceCodeAndStatusIn(String deviceCode, List<String> statuses);

    List<DeviceKeyEntity> findByStatus(String status);

    @Modifying
    @Query("DELETE FROM DeviceKeyEntity k WHERE k.deviceCode = :deviceCode")
    int deleteByDeviceCode(@Param("deviceCode") String deviceCode);
}
