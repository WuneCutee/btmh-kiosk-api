package vn.baotinmanhhai.kioskapi.price.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import vn.baotinmanhhai.kioskapi.catalog.adminclient.AdminCategoryDto;
import vn.baotinmanhhai.kioskapi.catalog.adminclient.AdminPortalClient;
import vn.baotinmanhhai.kioskapi.catalog.adminclient.AdminProductDto;
import vn.baotinmanhhai.kioskapi.catalog.service.ProductService;
import vn.baotinmanhhai.kioskapi.price.dto.PriceEventDto;
import vn.baotinmanhhai.kioskapi.price.dto.PriceItemDto;
import vn.baotinmanhhai.kioskapi.price.entity.PriceVersionEntity;
import vn.baotinmanhhai.kioskapi.price.repo.PriceVersionRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * II.3 - SSE. Dang ky MOT ket noi duy nhat moi deviceCode trong-tien-trinh (Map), dung gia dinh
 * single-instance da chot cho du an (xem README). Version dung chung, luu DB (PriceVersionEntity)
 * de song qua restart.
 *
 * <p>Nguon gia/ton la admin-portal: {@link #pollAdminPortal()} doc tuoi moi {@value #POLL_MS}ms (chi
 * khi co ket noi SSE), so voi lan truoc: doi tap san pham hoac thu tu -> {@code snapshot}; doi gia ban,
 * gia mua, ton hoac priceUpdatedAt -> {@code price} chi gom san pham doi. Xu huong UP/DOWN so voi gia
 * lan poll truoc; lan dau (hoac sau restart) la SAME - dung hanh vi spec cho truong hop chua co ban ghi truoc.
 */
@Service
public class PriceStreamService {

    private static final Logger log = LoggerFactory.getLogger(PriceStreamService.class);
    private static final long SSE_TIMEOUT_MS = 0L; // khong timeout, dua vao heartbeat/ket noi lai
    private static final long POLL_MS = 5_000L;
    private static final String TREND_UP = "UP";
    private static final String TREND_DOWN = "DOWN";
    private static final String TREND_SAME = "SAME";

    private final Map<String, SseEmitter> connections = new ConcurrentHashMap<>();
    private final AdminPortalClient adminPortalClient;
    private final ProductService productService;
    private final PriceVersionRepository priceVersionRepository;

    /** Trang thai lan doc gan nhat, theo dung thu tu hien thi; chi doi trong pollAdminPortal (synchronized). */
    private volatile Map<String, PriceItemDto> lastKnown = Map.of();
    /** Dau van tay noi dung (ten/mo ta/anh/ban vi/nhom) theo productId - doi thi gui snapshot de App tai lai danh sach. */
    private Map<String, String> lastContent = Map.of();
    /** Dau van tay danh muc lan doc gan nhat; null = chua co moc (lan dau chi lay moc, khong bao). */
    private List<String> lastCategories;

    public PriceStreamService(AdminPortalClient adminPortalClient, ProductService productService,
                              PriceVersionRepository priceVersionRepository) {
        this.adminPortalClient = adminPortalClient;
        this.productService = productService;
        this.priceVersionRepository = priceVersionRepository;
    }

    public SseEmitter connect(String deviceCode) {
        return connect(deviceCode, new SseEmitter(SSE_TIMEOUT_MS));
    }

    /** Tach rieng de test tiem emitter gia. */
    SseEmitter connect(String deviceCode, SseEmitter emitter) {
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
            List<PriceItemDto> items = snapshotItems();
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

    /** Snapshot giu xu huong da tinh o lan poll gan nhat (neu co), khong ve lai SAME cho ket noi moi. */
    private List<PriceItemDto> snapshotItems() {
        Map<String, PriceItemDto> current = toItems(productService.allEnabledPricedInListOrder(), lastKnown);
        return new ArrayList<>(current.values());
    }

    /**
     * Doc tuoi tu admin-portal va day su kien. Bo qua khi khong co ai nghe (khong ton request HMAC vo ich).
     * Loi admin-portal: AdminPortalClient tra ban cache cu -> khong phat hien thay doi, khong day gi.
     */
    @Scheduled(fixedDelay = POLL_MS, initialDelay = POLL_MS)
    public synchronized void pollAdminPortal() {
        if (connections.isEmpty()) {
            return;
        }
        pollCategories();
        try {
            adminPortalClient.refreshProducts();
        } catch (RuntimeException e) {
            log.warn("Khong doc duoc gia/ton tu admin-portal, bo qua lan poll nay: {}", e.getMessage());
            return;
        }
        List<AdminProductDto> products = productService.allEnabledPricedInListOrder();
        Map<String, PriceItemDto> previous = lastKnown;
        Map<String, String> previousContent = lastContent;
        Map<String, PriceItemDto> current = toItems(products, previous);
        Map<String, String> currentContent = contentFingerprints(products);
        lastKnown = current;
        lastContent = currentContent;

        Change change = classify(previous, previousContent, current, currentContent);
        switch (change.event()) {
            case "snapshot" -> {
                log.info("SSE: danh sach/thu tu hoac ten/anh/mo ta san pham doi -> snapshot");
                broadcast("snapshot", new PriceEventDto(nextVersion(), change.items()));
            }
            case "price" -> {
                log.info("SSE: {} san pham doi gia/ton -> price", change.items().size());
                broadcast("price", new PriceEventDto(nextVersion(), change.items()));
            }
            default -> {
            }
        }
    }

    /**
     * Danh muc doi (them/xoa/doi ten/doi uu tien) -> "config-changed" de App tai lai danh muc. Lan doc dau chi
     * lay moc. Loi admin-portal khong chan phan doc san pham phia sau (chi log warn).
     */
    private void pollCategories() {
        List<String> current;
        try {
            current = categoryFingerprint(adminPortalClient.refreshCategories());
        } catch (RuntimeException e) {
            log.warn("Khong doc duoc danh muc tu admin-portal, bo qua kiem tra danh muc lan nay: {}", e.getMessage());
            return;
        }
        List<String> previous = lastCategories;
        lastCategories = current;
        if (previous != null && !previous.equals(current)) {
            log.info("SSE: danh muc san pham doi -> config-changed");
            notifyConfigChanged();
        }
    }

    /** Dau van tay danh muc: (categoryCode|categoryName|priority), sap theo code de khong phu thuoc thu tu tra ve. */
    static List<String> categoryFingerprint(List<AdminCategoryDto> categories) {
        if (categories == null) {
            return List.of();
        }
        return categories.stream()
                .map(c -> String.join("|", String.valueOf(c.categoryCode()), String.valueOf(c.categoryName()),
                        String.valueOf(c.priority())))
                .sorted()
                .toList();
    }

    /** Ket qua so sanh 2 lan doc: event = "snapshot" | "price" | "none". */
    record Change(String event, List<PriceItemDto> items) {
    }

    /**
     * Lan dau (previous rong) -> none (chi lay moc). Doi tap/thu tu san pham HOAC noi dung hien thi (ten, anh,
     * mo ta, ban vi, nhom) -> snapshot toan bo (App tai lai danh sach). Chi doi gia ban/gia mua/ton/priceUpdatedAt
     * -> price gom dung cac san pham doi.
     */
    static Change classify(Map<String, PriceItemDto> previous, Map<String, String> previousContent,
                           Map<String, PriceItemDto> current, Map<String, String> currentContent) {
        if (previous.isEmpty()) {
            return new Change("none", List.of());
        }
        if (!new ArrayList<>(previous.keySet()).equals(new ArrayList<>(current.keySet()))
                || !previousContent.equals(currentContent)) {
            return new Change("snapshot", new ArrayList<>(current.values()));
        }
        List<PriceItemDto> changed = new ArrayList<>();
        for (PriceItemDto now : current.values()) {
            PriceItemDto before = previous.get(now.productId());
            if (now.price() != before.price()
                    || !Objects.equals(now.buyPrice(), before.buyPrice())
                    || now.remainingQuantity() != before.remainingQuantity()
                    || !Objects.equals(now.priceUpdatedAt(), before.priceUpdatedAt())) {
                changed.add(now);
            }
        }
        return changed.isEmpty() ? new Change("none", List.of()) : new Change("price", changed);
    }

    /** Dung danh sach item theo thu tu hien thi; xu huong so voi {@code previous} (khong doi gia -> giu xu huong cu). */
    static Map<String, PriceItemDto> toItems(List<AdminProductDto> products, Map<String, PriceItemDto> previous) {
        Map<String, PriceItemDto> items = new LinkedHashMap<>();
        int order = 1;
        for (AdminProductDto p : products) {
            long price = new BigDecimal(p.sell()).longValueExact();
            Long buyPrice = p.buy() == null ? null : new BigDecimal(p.buy()).longValueExact();
            PriceItemDto before = previous.get(p.productId());
            String priceTrend = before == null ? TREND_SAME
                    : price == before.price() ? before.priceTrend() : trend(price, before.price());
            String buyTrend;
            if (buyPrice == null || before == null || before.buyPrice() == null) {
                buyTrend = TREND_SAME;
            } else {
                buyTrend = buyPrice.equals(before.buyPrice()) ? before.buyPriceTrend() : trend(buyPrice, before.buyPrice());
            }
            items.put(p.productId(), new PriceItemDto(
                    p.productId(), p.productCode(), p.productName(), p.quantity(), price, buyPrice,
                    priceTrend, buyTrend, p.updatedAt(), order++, p.reserve() == null ? 0 : p.reserve()));
        }
        return items;
    }

    static Map<String, String> contentFingerprints(List<AdminProductDto> products) {
        Map<String, String> result = new LinkedHashMap<>();
        for (AdminProductDto p : products) {
            StringBuilder images = new StringBuilder();
            if (p.images() != null) {
                p.images().forEach(i -> images.append(i.url()).append('@').append(i.priority()).append(';'));
            }
            result.put(p.productId(), String.join("|",
                    String.valueOf(p.productName()), String.valueOf(p.description()), String.valueOf(p.quantity()),
                    String.valueOf(p.categoryCode()), images));
        }
        return result;
    }

    private static String trend(long now, long before) {
        return now > before ? TREND_UP : now < before ? TREND_DOWN : TREND_SAME;
    }

    /** Broadcast cho TAT CA ket noi dang mo khi danh sach san pham doi (II.3.1 snapshot). */
    public void broadcastSnapshot() {
        broadcast("snapshot", new PriceEventDto(nextVersion(), snapshotItems()));
    }

    public void notifyPlaylistChanged() {
        broadcast("playlist-changed", Map.of());
    }

    public void notifyConfigChanged() {
        broadcast("config-changed", Map.of());
    }

    /** Ban sao tap deviceCode dang mo SSE (trang thai "online" tren web admin). */
    public Set<String> connectedDeviceCodes() {
        return Set.copyOf(connections.keySet());
    }

    /** Dong SSE cua may (neu dang mo). KHONG BAO GIO nem loi: ket noi da chet thi coi nhu da dong. */
    public void revokeDevice(String deviceCode) {
        SseEmitter emitter = connections.remove(deviceCode);
        if (emitter != null) {
            sendAndClose(emitter, "device-revoked");
        }
    }

    private void sendReplacedAndClose(SseEmitter emitter) {
        sendAndClose(emitter, "replaced");
    }

    /**
     * complete() tren ket noi da loi (client ngat, response da hong) nem AsyncRequestNotUsableException/
     * IllegalStateException - truoc day loi nay lot ra ngoai lam connect()/revoke tra 500 (999).
     */
    private static void sendAndClose(SseEmitter emitter, String eventName) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(Map.of()));
        } catch (Exception ignored) {
            // ket noi da dong
        }
        try {
            emitter.complete();
        } catch (Exception e) {
            log.debug("SSE complete() loi tren ket noi da dong, bo qua: {}", e.toString());
        }
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
