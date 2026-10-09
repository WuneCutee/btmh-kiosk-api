package vn.baotinmanhhai.kioskapi.device.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.baotinmanhhai.kioskapi.device.entity.DeviceEntity;

public interface DeviceRepository extends JpaRepository<DeviceEntity, String> {
}
