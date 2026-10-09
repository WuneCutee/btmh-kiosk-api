package vn.baotinmanhhai.kioskapi.device.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.baotinmanhhai.kioskapi.device.entity.DeviceKeyEntity;

import java.util.List;
import java.util.Optional;

public interface DeviceKeyRepository extends JpaRepository<DeviceKeyEntity, String> {

    Optional<DeviceKeyEntity> findByDeviceCodeAndStatus(String deviceCode, String status);

    List<DeviceKeyEntity> findByDeviceCode(String deviceCode);

    List<DeviceKeyEntity> findByDeviceCodeAndStatusIn(String deviceCode, List<String> statuses);
}
