package vn.baotinmanhhai.kioskapi.catalog.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.baotinmanhhai.kioskapi.catalog.entity.ChannelConfigEntity;

public interface ChannelConfigRepository extends JpaRepository<ChannelConfigEntity, Long> {
}
