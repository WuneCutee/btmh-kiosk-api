package vn.baotinmanhhai.kioskapi.ops;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.baotinmanhhai.kioskapi.common.ApiException;
import vn.baotinmanhhai.kioskapi.common.ErrorCodes;
import vn.baotinmanhhai.kioskapi.common.TimeUtil;
import vn.baotinmanhhai.kioskapi.device.entity.DeviceEntity;
import vn.baotinmanhhai.kioskapi.device.entity.DeviceKeyEntity;
import vn.baotinmanhhai.kioskapi.device.repo.ActivationCodeRepository;
import vn.baotinmanhhai.kioskapi.device.repo.DeviceKeyRepository;
import vn.baotinmanhhai.kioskapi.device.repo.DeviceNonceRepository;
import vn.baotinmanhhai.kioskapi.device.repo.DeviceRepository;
import vn.baotinmanhhai.kioskapi.ops.dto.OpsDeviceDto;
import vn.baotinmanhhai.kioskapi.ops.dto.OpsDeviceUpdateRequest;
import vn.baotinmanhhai.kioskapi.price.service.PriceStreamService;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Quan tri may kiosk cho web admin. Chi lam phan DB trong transaction; viec dong SSE / bao
 * config-changed do controller goi SAU khi transaction da commit (loi SSE khong duoc lam rollback
 * hay bien thao tac DB thanh 500).
 */
@Service
public class OpsDeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceKeyRepository deviceKeyRepository;
    private final ActivationCodeRepository activationCodeRepository;
    private final DeviceNonceRepository deviceNonceRepository;
    private final PriceStreamService priceStreamService;

    public OpsDeviceService(DeviceRepository deviceRepository, DeviceKeyRepository deviceKeyRepository,
                            ActivationCodeRepository activationCodeRepository,
                            DeviceNonceRepository deviceNonceRepository, PriceStreamService priceStreamService) {
        this.deviceRepository = deviceRepository;
        this.deviceKeyRepository = deviceKeyRepository;
        this.activationCodeRepository = activationCodeRepository;
        this.deviceNonceRepository = deviceNonceRepository;
        this.priceStreamService = priceStreamService;
    }

    @Transactional(readOnly = true)
    public List<OpsDeviceDto> list() {
        Map<String, DeviceKeyEntity> activeKeys = deviceKeyRepository.findByStatus(DeviceKeyEntity.STATUS_ACTIVE).stream()
                .collect(Collectors.toMap(DeviceKeyEntity::getDeviceCode, Function.identity(),
                        (a, b) -> a.getCreatedAt().isAfter(b.getCreatedAt()) ? a : b));
        Set<String> online = priceStreamService.connectedDeviceCodes();
        return deviceRepository.findAll().stream()
                .sorted(Comparator.comparing(DeviceEntity::getCreatedAt,
                        Comparator.nullsLast(Comparator.<Instant>naturalOrder())).reversed())
                .map(d -> toDto(d, activeKeys.get(d.getDeviceCode()), online))
                .toList();
    }

    @Transactional(readOnly = true)
    public OpsDeviceDto get(String deviceCode) {
        return toDto(find(deviceCode));
    }

    @Transactional
    public OpsDeviceDto update(String deviceCode, OpsDeviceUpdateRequest req) {
        DeviceEntity device = find(deviceCode);
        device.setStoreId(req.storeId());
        device.setStoreName(req.storeName().trim());
        return toDto(deviceRepository.save(device));
    }

    @Transactional
    public OpsDeviceDto forceKeyRotation(String deviceCode) {
        DeviceEntity device = find(deviceCode);
        if (!device.isActive()) {
            throw ErrorCodes.deviceNotActive();
        }
        device.setForceKeyRotation(true);
        return toDto(deviceRepository.save(device));
    }

    @Transactional
    public void revoke(String deviceCode) {
        DeviceEntity device = find(deviceCode);
        device.setStatus(DeviceEntity.STATUS_REVOKED);
        deviceRepository.save(device);
    }

    /** Xoa may + khoa + ma kich hoat + nonce trong 1 transaction; GIU log loi (kiosk_device_error_log). */
    @Transactional
    public void delete(String deviceCode) {
        DeviceEntity device = find(deviceCode);
        deviceKeyRepository.deleteByDeviceCode(deviceCode);
        activationCodeRepository.deleteByDeviceCode(deviceCode);
        deviceNonceRepository.deleteByDeviceCode(deviceCode);
        deviceRepository.delete(device);
    }

    private DeviceEntity find(String deviceCode) {
        return deviceRepository.findById(deviceCode)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "404", "Khong tim thay may: " + deviceCode));
    }

    private OpsDeviceDto toDto(DeviceEntity d) {
        DeviceKeyEntity key = deviceKeyRepository
                .findByDeviceCodeAndStatusIn(d.getDeviceCode(), List.of(DeviceKeyEntity.STATUS_ACTIVE)).stream()
                .max(Comparator.comparing(DeviceKeyEntity::getCreatedAt))
                .orElse(null);
        return toDto(d, key, priceStreamService.connectedDeviceCodes());
    }

    static OpsDeviceDto toDto(DeviceEntity d, DeviceKeyEntity activeKey, Set<String> online) {
        return new OpsDeviceDto(
                d.getDeviceCode(),
                d.getStoreId() == null ? 0L : d.getStoreId(),
                d.getStoreName(),
                d.getStatus(),
                d.getAppVersion(),
                d.getOsVersion(),
                d.getDeviceModel(),
                iso(d.getCreatedAt()),
                iso(d.getActivatedAt()),
                iso(d.getLastRotatedAt()),
                d.isForceKeyRotation(),
                online.contains(d.getDeviceCode()),
                activeKey == null ? null : activeKey.getKeyId(),
                activeKey == null ? null : activeKey.getPublicKey());
    }

    private static String iso(Instant instant) {
        return instant == null ? null
                : instant.atZone(TimeUtil.VIETNAM_ZONE).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
