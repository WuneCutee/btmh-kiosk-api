package vn.baotinmanhhai.kioskapi.device.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.baotinmanhhai.kioskapi.device.entity.DeviceErrorLogEntity;

public interface DeviceErrorLogRepository extends JpaRepository<DeviceErrorLogEntity, String> {

    boolean existsByDeviceCodeAndErrorId(String deviceCode, String errorId);
}
