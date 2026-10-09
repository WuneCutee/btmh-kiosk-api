package vn.baotinmanhhai.kioskapi.catalog.service;

import org.springframework.stereotype.Service;
import vn.baotinmanhhai.kioskapi.catalog.dto.BootstrapDto;
import vn.baotinmanhhai.kioskapi.catalog.dto.SystemConfigDto;
import vn.baotinmanhhai.kioskapi.catalog.entity.ChannelConfigEntity;
import vn.baotinmanhhai.kioskapi.common.ErrorCodes;
import vn.baotinmanhhai.kioskapi.common.TimeUtil;
import vn.baotinmanhhai.kioskapi.device.entity.DeviceEntity;
import vn.baotinmanhhai.kioskapi.device.repo.DeviceRepository;

import java.time.LocalTime;

/** II.2.3 (legacy) + II.2.12 (bootstrap). */
@Service
public class BootstrapService {

    private final ChannelConfigService channelConfigService;
    private final DeviceRepository deviceRepository;

    public BootstrapService(ChannelConfigService channelConfigService, DeviceRepository deviceRepository) {
        this.channelConfigService = channelConfigService;
        this.deviceRepository = deviceRepository;
    }

    public SystemConfigDto legacySystemConfig() {
        ChannelConfigEntity cfg = channelConfigService.get();
        return new SystemConfigDto(toHms(cfg.getOpenTime()), toHms(cfg.getCloseTime()), cfg.isEnableHkd());
    }

    public BootstrapDto bootstrap(String deviceCode) {
        ChannelConfigEntity cfg = channelConfigService.get();
        DeviceEntity device = deviceRepository.findById(deviceCode).orElseThrow(ErrorCodes::unauthorized);
        return new BootstrapDto(
                toHms(cfg.getOpenTime()), toHms(cfg.getCloseTime()), cfg.getHotline(),
                cfg.getIdleTimeoutSeconds(), cfg.getStalePriceSeconds(), cfg.getMinAppVersion(),
                TimeUtil.nowEpochMillis(), device.getStoreId(), device.getStoreName()
        );
    }

    private static String toHms(LocalTime time) {
        return time == null ? null : time.toString().length() == 5 ? time.toString() + ":00" : time.toString();
    }
}
