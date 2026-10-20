package vn.baotinmanhhai.kioskapi.device.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.baotinmanhhai.kioskapi.common.ApiException;
import vn.baotinmanhhai.kioskapi.common.ErrorCodes;
import vn.baotinmanhhai.kioskapi.device.entity.ActivationCodeEntity;
import vn.baotinmanhhai.kioskapi.device.entity.DeviceEntity;
import vn.baotinmanhhai.kioskapi.device.repo.ActivationCodeRepository;
import vn.baotinmanhhai.kioskapi.device.repo.DeviceRepository;
import vn.baotinmanhhai.kioskapi.device.util.HashUtil;
import org.springframework.http.HttpStatus;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Trien khai lai (khong import cheo) thuat toan sinh ma kich hoat cua
 * kiosk-admin KioskDeviceService.generateActivationCode()/issueActivationCode(): 12 ky tu base32
 * chu hoa, luu ban SHA-256 (khong luu plaintext), han 24h, dem attempts, huy sau 5 lan sai (xu ly
 * o DeviceAuthService.enroll). Dung cho endpoint noi bo /internal/ops/devices (xem OpsDeviceController).
 */
@Service
public class ActivationCodeService {

    // Phai khop bang ky tu App chap nhan (ActivationInput.kt: khong co I/L/O/U, co 0/1; App tu doi
    // I/L->1, O->0 va bo ky tu ngoai bang) - Base32 chuan cu sinh ma chua I/O/U/L khong go duoc.
    private static final String BASE32_ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
    private static final int ACTIVATION_CODE_LENGTH = 12;
    private static final Duration ACTIVATION_CODE_TTL = Duration.ofHours(24);

    private final DeviceRepository deviceRepository;
    private final ActivationCodeRepository activationCodeRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public ActivationCodeService(DeviceRepository deviceRepository, ActivationCodeRepository activationCodeRepository) {
        this.deviceRepository = deviceRepository;
        this.activationCodeRepository = activationCodeRepository;
    }

    public record IssuedActivationCode(String deviceCode, String activationCode, Instant expiresAt) {
    }

    @Transactional
    public IssuedActivationCode createPendingDeviceAndIssueCode(String deviceCode, long storeId, String storeName) {
        if (deviceRepository.existsById(deviceCode)) {
            throw new ApiException(HttpStatus.CONFLICT, "409", "deviceCode da ton tai: " + deviceCode);
        }
        DeviceEntity device = new DeviceEntity();
        device.setDeviceCode(deviceCode);
        device.setStoreId(storeId);
        device.setStoreName(storeName);
        device.setStatus(DeviceEntity.STATUS_PENDING);
        device.setCreatedAt(Instant.now());
        device.setForceKeyRotation(false);
        deviceRepository.save(device);
        return issueActivationCode(deviceCode);
    }

    /**
     * Cap lai ma kich hoat cho may PENDING (ma cu het han/that lac): huy moi ma chua dung con hieu luc
     * roi phat ma moi. May khong ton tai -> 404; khong o PENDING (ACTIVE/REVOKED) -> 409 code 122.
     */
    @Transactional
    public IssuedActivationCode reissueActivationCode(String deviceCode) {
        DeviceEntity device = deviceRepository.findById(deviceCode)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "404", "Khong tim thay may: " + deviceCode));
        if (!device.isPending()) {
            throw ErrorCodes.deviceNotPending();
        }
        for (ActivationCodeEntity old : activationCodeRepository.findByDeviceCodeAndUsedFalseAndRevokedFalse(deviceCode)) {
            old.setRevoked(true);
            activationCodeRepository.save(old);
        }
        return issueActivationCode(deviceCode);
    }

    private IssuedActivationCode issueActivationCode(String deviceCode) {
        String code = generateActivationCode();
        Instant expiresAt = Instant.now().plus(ACTIVATION_CODE_TTL);
        ActivationCodeEntity entity = new ActivationCodeEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setDeviceCode(deviceCode);
        entity.setCodeHash(HashUtil.sha256Hex(code));
        entity.setExpiresAt(expiresAt);
        entity.setUsed(false);
        entity.setRevoked(false);
        entity.setAttempts(0);
        entity.setCreatedAt(Instant.now());
        activationCodeRepository.save(entity);
        return new IssuedActivationCode(deviceCode, code, expiresAt);
    }

    private String generateActivationCode() {
        StringBuilder sb = new StringBuilder(ACTIVATION_CODE_LENGTH);
        for (int i = 0; i < ACTIVATION_CODE_LENGTH; i++) {
            sb.append(BASE32_ALPHABET.charAt(secureRandom.nextInt(BASE32_ALPHABET.length())));
        }
        return sb.toString();
    }
}
