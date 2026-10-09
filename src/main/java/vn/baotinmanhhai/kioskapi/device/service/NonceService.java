package vn.baotinmanhhai.kioskapi.device.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.baotinmanhhai.kioskapi.common.ErrorCodes;
import vn.baotinmanhhai.kioskapi.device.entity.DeviceNonceEntity;
import vn.baotinmanhhai.kioskapi.device.repo.DeviceNonceRepository;

import java.time.Duration;
import java.time.Instant;

/**
 * Chong phat lai nonce (10 phut gan nhat). Goi reject(...) CHI SAU KHI chu ky da verify thanh
 * cong (dung thu tu kiem o II.5.2) - cac controller phai goi theo dung trinh tu nay, service nay
 * khong tu sap xep lai.
 */
@Service
public class NonceService {

    private static final Logger log = LoggerFactory.getLogger(NonceService.class);
    private static final Duration REPLAY_WINDOW = Duration.ofMinutes(10);

    private final DeviceNonceRepository repository;

    public NonceService(DeviceNonceRepository repository) {
        this.repository = repository;
    }

    /** Throw 401 code 103 neu nonce da dung trong 10 phut gan nhat; neu khong, GHI nonce ngay (ghi-doc nguyen tu o muc co ban). */
    @Transactional
    public void checkAndRecord(String deviceCode, String nonce) {
        Instant cutoff = Instant.now().minus(REPLAY_WINDOW);
        try {
            boolean reused = repository.existsByIdDeviceCodeAndIdNonceAndCreatedAtAfter(deviceCode, nonce, cutoff);
            if (reused) {
                throw ErrorCodes.nonceReused();
            }
            repository.save(new DeviceNonceEntity(deviceCode, nonce, Instant.now()));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Dung nonce trong khoang thoi gian rat gan nhau (race) -> coi la da dung.
            throw ErrorCodes.nonceReused();
        }
    }

    @Scheduled(fixedRate = 5 * 60 * 1000L)
    public void cleanupOldNonces() {
        try {
            int deleted = repository.deleteOlderThan(Instant.now().minus(REPLAY_WINDOW));
            if (deleted > 0) {
                log.debug("Da xoa {} nonce cu (qua 10 phut)", deleted);
            }
        } catch (Exception e) {
            log.warn("Khong don dep duoc bang nonce", e);
        }
    }
}
