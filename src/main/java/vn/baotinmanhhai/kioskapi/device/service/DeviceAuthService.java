package vn.baotinmanhhai.kioskapi.device.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.baotinmanhhai.kioskapi.auth.DeviceSignatureService;
import vn.baotinmanhhai.kioskapi.auth.JwtService;
import vn.baotinmanhhai.kioskapi.catalog.service.ChannelConfigService;
import vn.baotinmanhhai.kioskapi.common.ErrorCodes;
import vn.baotinmanhhai.kioskapi.common.TimeUtil;
import vn.baotinmanhhai.kioskapi.device.dto.*;
import vn.baotinmanhhai.kioskapi.device.entity.ActivationCodeEntity;
import vn.baotinmanhhai.kioskapi.device.entity.DeviceEntity;
import vn.baotinmanhhai.kioskapi.device.entity.DeviceKeyEntity;
import vn.baotinmanhhai.kioskapi.device.repo.ActivationCodeRepository;
import vn.baotinmanhhai.kioskapi.device.repo.DeviceKeyRepository;
import vn.baotinmanhhai.kioskapi.device.repo.DeviceRepository;
import vn.baotinmanhhai.kioskapi.device.util.HashUtil;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Trien khai 3 API xac thuc thiet bi (II.2.1, II.2.14, II.2.15), theo dung thu tu kiem chung o
 * II.5.2: (1) trace id - da xu ly o TraceIdFilter, (2) body - da xu ly boi @Valid o controller,
 * (3) rate limit - controller goi truoc khi vao day, (4) timestamp +-5p, (5) tim khoa, (6) chu
 * ky, (7) ghi nonce (CHI SAU KHI chu ky dung), (8) trang thai may, (9) logic rieng cua API.
 */
@Service
public class DeviceAuthService {

    private static final Logger log = LoggerFactory.getLogger(DeviceAuthService.class);
    private static final Duration CLOCK_SKEW_ALLOWED = Duration.ofMinutes(5);
    private static final Duration ROTATION_MIN_INTERVAL = Duration.ofHours(1);
    private static final Duration ROTATION_OVERLAP = Duration.ofMinutes(10);
    private static final int MAX_ACTIVATION_ATTEMPTS = 5;
    private static final DateTimeFormatter ISO_OFFSET = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final DeviceRepository deviceRepository;
    private final DeviceKeyRepository deviceKeyRepository;
    private final ActivationCodeRepository activationCodeRepository;
    private final DeviceSignatureService signatureService;
    private final JwtService jwtService;
    private final NonceService nonceService;
    private final ChannelConfigService channelConfigService;

    public DeviceAuthService(DeviceRepository deviceRepository, DeviceKeyRepository deviceKeyRepository,
                              ActivationCodeRepository activationCodeRepository,
                              DeviceSignatureService signatureService, JwtService jwtService,
                              NonceService nonceService, ChannelConfigService channelConfigService) {
        this.deviceRepository = deviceRepository;
        this.deviceKeyRepository = deviceKeyRepository;
        this.activationCodeRepository = activationCodeRepository;
        this.signatureService = signatureService;
        this.jwtService = jwtService;
        this.nonceService = nonceService;
        this.channelConfigService = channelConfigService;
    }

    private void checkClockSkew(long timestamp) {
        long now = TimeUtil.nowEpochMillis();
        if (Math.abs(now - timestamp) > CLOCK_SKEW_ALLOWED.toMillis()) {
            throw ErrorCodes.clockSkew(now);
        }
    }

    // ---------------------------------------------------------------- II.2.1 ----

    @Transactional
    public DeviceSessionResponse issueToken(DeviceSessionRequest req) {
        checkClockSkew(req.getTimestamp());

        List<DeviceKeyEntity> candidates = deviceKeyRepository.findByDeviceCodeAndStatusIn(
                req.getDeviceCode(), List.of(DeviceKeyEntity.STATUS_ACTIVE, DeviceKeyEntity.STATUS_RETIRING));
        candidates.removeIf(k -> k.getStatus().equals(DeviceKeyEntity.STATUS_RETIRING)
                && (k.getRetiringUntil() == null || k.getRetiringUntil().isBefore(Instant.now())));
        if (candidates.isEmpty()) {
            throw ErrorCodes.keyNotRecognized();
        }

        String baseString = DeviceSignatureService.buildBaseString(
                req.getDeviceCode(), String.valueOf(req.getTimestamp()), req.getNonce());

        DeviceKeyEntity matched = candidates.stream()
                .filter(k -> signatureService.verify(baseString, req.getSignature(), k.getPublicKey()))
                .findFirst()
                .orElse(null);
        if (matched == null) {
            throw ErrorCodes.invalidSignature();
        }

        nonceService.checkAndRecord(req.getDeviceCode(), req.getNonce());

        DeviceEntity device = deviceRepository.findById(req.getDeviceCode())
                .orElseThrow(ErrorCodes::keyNotRecognized);
        if (device.isRevoked()) {
            throw ErrorCodes.deviceRevoked();
        }
        if (!device.isActive()) {
            throw ErrorCodes.keyNotRecognized();
        }

        // Khoa dang dung la ACTIVE -> may da chuyen sang dung khoa moi thanh cong; tat RETIRING cu.
        if (matched.getStatus().equals(DeviceKeyEntity.STATUS_ACTIVE)) {
            deviceKeyRepository.findByDeviceCodeAndStatus(device.getDeviceCode(), DeviceKeyEntity.STATUS_RETIRING)
                    .ifPresent(old -> {
                        old.setStatus(DeviceKeyEntity.STATUS_RETIRED);
                        old.setRetiredAt(Instant.now());
                        deviceKeyRepository.save(old);
                    });
        }

        device.setAppVersion(req.getAppVersion());
        device.setOsVersion(req.getOsVersion());
        deviceRepository.save(device);

        JwtService.IssuedToken issued = jwtService.issue(device.getDeviceCode(), device.getStoreId(), matched.getKeyId());
        boolean rotationNeeded = device.isForceKeyRotation()
                || Duration.between(matched.getCreatedAt(), Instant.now()).compareTo(Duration.ofDays(365)) > 0;

        return new DeviceSessionResponse(
                issued.token(),
                "Bearer",
                JwtService.EXPIRES_IN_SECONDS,
                formatIso(Instant.ofEpochSecond(issued.exp())),
                TimeUtil.nowEpochMillis(),
                device.getStoreId(),
                device.getStoreName(),
                channelConfigService.get().getMinAppVersion(),
                rotationNeeded
        );
    }

    // ---------------------------------------------------------------- II.2.14 ----

    public record EnrollResult(EnrollResponse response, boolean created) {
    }

    @Transactional
    public EnrollResult enroll(EnrollRequest req) {
        checkClockSkew(req.getTimestamp());

        String baseString = DeviceSignatureService.buildBaseString(
                req.getDeviceCode(), String.valueOf(req.getTimestamp()), req.getNonce(),
                req.getActivationCode(), req.getPublicKey());

        boolean sigOk = signatureService.verify(baseString, req.getSignature(), req.getPublicKey());
        if (!sigOk) {
            throw ErrorCodes.invalidSignature();
        }

        nonceService.checkAndRecord(req.getDeviceCode(), req.getNonce());

        DeviceEntity device = deviceRepository.findById(req.getDeviceCode())
                .orElseThrow(ErrorCodes::activationCodeInvalid);

        if (device.isActive()) {
            Optional<DeviceKeyEntity> activeKey = deviceKeyRepository
                    .findByDeviceCodeAndStatus(device.getDeviceCode(), DeviceKeyEntity.STATUS_ACTIVE);
            if (activeKey.isPresent() && activeKey.get().getPublicKey().equals(req.getPublicKey())) {
                // Goi lai sau mat mang voi dung khoa da dang ky - coi la lap lai, tra 200.
                return new EnrollResult(new EnrollResponse(
                        device.getDeviceCode(), "ACTIVE", formatIso(device.getActivatedAt()), TimeUtil.nowEpochMillis()
                ), false);
            }
            throw ErrorCodes.publicKeyMismatch();
        }
        if (device.isRevoked()) {
            throw ErrorCodes.deviceNotPending();
        }
        if (!device.isPending()) {
            throw ErrorCodes.deviceNotPending();
        }

        ActivationCodeEntity code = activationCodeRepository
                .findFirstByDeviceCodeAndUsedFalseAndRevokedFalseOrderByCreatedAtDesc(device.getDeviceCode())
                .orElseThrow(ErrorCodes::activationCodeInvalid);

        if (code.isRevoked()) {
            throw ErrorCodes.activationCodeRevoked();
        }
        if (code.getExpiresAt().isBefore(Instant.now())) {
            throw ErrorCodes.activationCodeExpired();
        }
        String givenHash = HashUtil.sha256Hex(req.getActivationCode());
        if (!HashUtil.constantTimeEquals(givenHash, code.getCodeHash())) {
            code.setAttempts(code.getAttempts() + 1);
            if (code.getAttempts() >= MAX_ACTIVATION_ATTEMPTS) {
                code.setRevoked(true);
            }
            activationCodeRepository.save(code);
            throw ErrorCodes.activationCodeInvalid();
        }

        Instant now = Instant.now();
        code.setUsed(true);
        code.setUsedAt(now);
        activationCodeRepository.save(code);

        DeviceKeyEntity key = new DeviceKeyEntity();
        key.setKeyId(UUID.randomUUID().toString());
        key.setDeviceCode(device.getDeviceCode());
        key.setPublicKey(req.getPublicKey());
        key.setStatus(DeviceKeyEntity.STATUS_ACTIVE);
        key.setSource(DeviceKeyEntity.SOURCE_ENROLL);
        key.setCreatedAt(now);
        deviceKeyRepository.save(key);

        device.setStatus(DeviceEntity.STATUS_ACTIVE);
        device.setActivatedAt(now);
        device.setAppVersion(req.getAppVersion());
        device.setOsVersion(req.getOsVersion());
        device.setDeviceModel(req.getDeviceModel());
        deviceRepository.save(device);

        log.info("Kich hoat may thanh cong: deviceCode={}", device.getDeviceCode());

        return new EnrollResult(new EnrollResponse(
                device.getDeviceCode(), "ACTIVE", formatIso(now), TimeUtil.nowEpochMillis()
        ), true);
    }

    // ---------------------------------------------------------------- II.2.15 ----

    @Transactional
    public RotateKeyResponse rotateKey(RotateKeyRequest req) {
        checkClockSkew(req.getTimestamp());

        String baseString = DeviceSignatureService.buildBaseString(
                req.getDeviceCode(), String.valueOf(req.getTimestamp()), req.getNonce(), req.getNewPublicKey());

        List<DeviceKeyEntity> candidates = deviceKeyRepository.findByDeviceCodeAndStatusIn(
                req.getDeviceCode(), List.of(DeviceKeyEntity.STATUS_ACTIVE, DeviceKeyEntity.STATUS_RETIRING));
        candidates.removeIf(k -> k.getStatus().equals(DeviceKeyEntity.STATUS_RETIRING)
                && (k.getRetiringUntil() == null || k.getRetiringUntil().isBefore(Instant.now())));
        if (candidates.isEmpty()) {
            throw ErrorCodes.keyNotRecognized();
        }
        DeviceKeyEntity currentActive = candidates.stream()
                .filter(k -> k.getStatus().equals(DeviceKeyEntity.STATUS_ACTIVE))
                .findFirst()
                .orElseThrow(ErrorCodes::keyNotRecognized);

        boolean oldSigOk = candidates.stream()
                .anyMatch(k -> signatureService.verify(baseString, req.getSignature(), k.getPublicKey()));
        if (!oldSigOk) {
            throw ErrorCodes.invalidSignature();
        }
        boolean newSigOk = signatureService.verify(baseString, req.getNewKeySignature(), req.getNewPublicKey());
        if (!newSigOk) {
            throw ErrorCodes.invalidSignature();
        }

        nonceService.checkAndRecord(req.getDeviceCode(), req.getNonce());

        DeviceEntity device = deviceRepository.findById(req.getDeviceCode())
                .orElseThrow(ErrorCodes::keyNotRecognized);
        if (device.isRevoked()) {
            throw ErrorCodes.deviceRevoked();
        }
        if (!device.isActive()) {
            throw ErrorCodes.deviceNotActive();
        }

        Optional<DeviceKeyEntity> retiring = candidates.stream()
                .filter(k -> k.getStatus().equals(DeviceKeyEntity.STATUS_RETIRING))
                .findFirst();
        if (retiring.isPresent()) {
            if (currentActive.getPublicKey().equals(req.getNewPublicKey())) {
                return new RotateKeyResponse(device.getDeviceCode(), formatIso(retiring.get().getRetiringUntil()),
                        TimeUtil.nowEpochMillis());
            }
            throw ErrorCodes.rotationInProgressWithDifferentKey();
        }

        if (DeviceKeyEntity.SOURCE_ROTATE.equals(currentActive.getSource())
                && Duration.between(currentActive.getCreatedAt(), Instant.now()).compareTo(ROTATION_MIN_INTERVAL) < 0) {
            throw ErrorCodes.rotationTooSoon();
        }

        Instant now = Instant.now();
        Instant overlapUntil = now.plus(ROTATION_OVERLAP);

        currentActive.setStatus(DeviceKeyEntity.STATUS_RETIRING);
        currentActive.setRetiringUntil(overlapUntil);
        deviceKeyRepository.save(currentActive);

        DeviceKeyEntity newKey = new DeviceKeyEntity();
        newKey.setKeyId(UUID.randomUUID().toString());
        newKey.setDeviceCode(device.getDeviceCode());
        newKey.setPublicKey(req.getNewPublicKey());
        newKey.setStatus(DeviceKeyEntity.STATUS_ACTIVE);
        newKey.setSource(DeviceKeyEntity.SOURCE_ROTATE);
        newKey.setCreatedAt(now);
        deviceKeyRepository.save(newKey);

        device.setLastRotatedAt(now);
        device.setForceKeyRotation(false);
        deviceRepository.save(device);

        log.info("Xoay khoa thanh cong: deviceCode={} oldKeyId={} newKeyId={}",
                device.getDeviceCode(), currentActive.getKeyId(), newKey.getKeyId());

        return new RotateKeyResponse(device.getDeviceCode(), formatIso(overlapUntil), TimeUtil.nowEpochMillis());
    }

    @Scheduled(fixedRate = 60_000L)
    public void retireExpiredOverlapKeys() {
        try {
            Instant now = Instant.now();
            // Khong co truy van "findAllRetiringExpired" chuyen dung o repo de giu don gian; quet toan bo
            // la chap nhan duoc o quy mo 1 kiosk chain nho.
            deviceKeyRepository.findAll().stream()
                    .filter(k -> DeviceKeyEntity.STATUS_RETIRING.equals(k.getStatus()))
                    .filter(k -> k.getRetiringUntil() != null && k.getRetiringUntil().isBefore(now))
                    .forEach(k -> {
                        k.setStatus(DeviceKeyEntity.STATUS_RETIRED);
                        k.setRetiredAt(now);
                        deviceKeyRepository.save(k);
                    });
        } catch (Exception e) {
            log.warn("Khong don dep duoc khoa RETIRING het han", e);
        }
    }

    private static String formatIso(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(TimeUtil.VIETNAM_ZONE).format(ISO_OFFSET);
    }
}
