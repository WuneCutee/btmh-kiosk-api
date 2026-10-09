package vn.baotinmanhhai.kioskapi.price.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import vn.baotinmanhhai.kioskapi.catalog.entity.ProductEntity;
import vn.baotinmanhhai.kioskapi.catalog.repo.ProductRepository;
import vn.baotinmanhhai.kioskapi.common.TimeUtil;
import vn.baotinmanhhai.kioskapi.price.dto.PriceEventDto;
import vn.baotinmanhhai.kioskapi.price.dto.PriceItemDto;
import vn.baotinmanhhai.kioskapi.price.entity.PriceVersionEntity;
import vn.baotinmanhhai.kioskapi.price.repo.PriceVersionRepository;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * II.3 - SSE. Dang ky MOT ket noi duy nhat moi deviceCode trong-tien-trinh (Map), dung gia dinh
 * single-instance da chot cho du an (xem README). Version dung chung, luu DB (PriceVersionEntity)
 * de song qua restart.
 */
@Service
public class PriceStreamService {

    private static final Logger log = LoggerFactory.getLogger(PriceStreamService.class);
    private static final long SSE_TIMEOUT_MS = 0L; // khong timeout, dua vao heartbeat/ket noi lai

    private final Map<String, SseEmitter> connections = new ConcurrentHashMap<>();
    private final ProductRepository productRepository;
    private final PriceVersionRepository priceVersionRepository;

    public PriceStreamService(ProductRepository productRepository, PriceVersionRepository priceVersionRepository) {
        this.productRepository = productRepository;
        this.priceVersionRepository = priceVersionRepository;
    }

    public SseEmitter connect(String deviceCode) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        SseEmitter replaced = connections.put(deviceCode, emitter);
        if (replaced != null) {
            sendReplacedAndClose(replaced);
        }
        emitter.onCompletion(() -> connections.remove(deviceCode, emitter));
        emitter.onTimeout(() -> connections.remove(deviceCode, emitter));
        emitter.onError(e -> connections.remove(deviceCode, emitter));

        sendSnapshot(emitter);
        return emitter;
    }

    @Transactional
    public void sendSnapshot(SseEmitter emitter) {
        try {
            long version = currentVersion();
            List<PriceItemDto> items = buildSnapshotItems();
            emitter.send(SseEmitter.event().name("snapshot").data(new PriceEventDto(version, items)));
        } catch (Exception e) {
            log.debug("Khong gui duoc snapshot SSE (ket noi da dong?)", e);
        }
    }

    @Transactional
    public long currentVersion() {
        return priceVersionRepository.findById(PriceVersionEntity.SINGLETON_ID)
                .map(PriceVersionEntity::getValue)
                .orElseGet(() -> priceVersionRepository.save(new PriceVersionEntity()).getValue());
    }

    @Transactional
    public long nextVersion() {
        priceVersionRepository.incrementRow();
        return currentVersion();
    }

    private List<PriceItemDto> buildSnapshotItems() {
        List<ProductEntity> products = productRepository.findAllForPriceSnapshot();
        List<PriceItemDto> items = new java.util.ArrayList<>();
        int order = 1;
        for (ProductEntity p : products) {
            items.add(new PriceItemDto(
                    p.getId(), p.getCode(), p.getName(), p.getQuantity(), p.getPrice(), p.getBuyPrice(),
                    p.getPriceTrend(), p.getBuyPriceTrend(),
                    p.getPriceUpdatedAt().atZone(TimeUtil.VIETNAM_ZONE).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                    order++, p.getRemainingQuantity()
            ));
        }
        return items;
    }

    /** Broadcast cho TAT CA ket noi dang mo khi danh sach san pham doi (II.3.1 snapshot). */
    public void broadcastSnapshot() {
        long version = nextVersion();
        List<PriceItemDto> items = buildSnapshotItems();
        PriceEventDto event = new PriceEventDto(version, items);
        broadcast("snapshot", event);
    }

    /** Broadcast chi cac san pham doi gia/ton (II.3.1 price). Khong co nguon gia thuc trong pham vi du an nay (xem README). */
    public void broadcastPriceChange(List<PriceItemDto> changedItems) {
        long version = nextVersion();
        broadcast("price", new PriceEventDto(version, changedItems));
    }

    public void notifyPlaylistChanged() {
        broadcast("playlist-changed", Map.of());
    }

    public void notifyConfigChanged() {
        broadcast("config-changed", Map.of());
    }

    public void revokeDevice(String deviceCode) {
        SseEmitter emitter = connections.remove(deviceCode);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("device-revoked").data(Map.of()));
            } catch (Exception ignored) {
            }
            emitter.complete();
        }
    }

    private void sendReplacedAndClose(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("replaced").data(Map.of()));
        } catch (Exception ignored) {
        }
        emitter.complete();
    }

    private void broadcast(String eventName, Object data) {
        connections.forEach((deviceCode, emitter) -> {
            if (emitter == null) {
                return;
            }
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (Exception e) {
                connections.remove(deviceCode, emitter);
            }
        });
    }

    @Scheduled(fixedRate = 15_000L)
    public void heartbeat() {
        connections.forEach((deviceCode, emitter) -> {
            if (emitter == null) {
                return;
            }
            try {
                emitter.send(SseEmitter.event().comment("ping"));
            } catch (Exception e) {
                connections.remove(deviceCode, emitter);
            }
        });
    }
}
