package vn.baotinmanhhai.kioskapi.catalog.adminclient;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Doc du lieu catalog (san pham/nhom/cua hang/han muc) tu admin-portal qua bo API partner-HMAC
 * (GET /api/products|categories|stores|limits) - admin-portal la system-of-record duy nhat
 * (quyet dinh 15/10/2026, xem README muc "Nguon catalog"). KHONG dung Redis - cache TTL
 * trong-tien-trinh (ConcurrentHashMap-style, 1 slot/endpoint), dung triet ly "single-instance,
 * khong message broker" da ghi trong README tu luc khoi tao project.
 *
 * <p>Loi goi admin-portal (timeout/5xx/parse) duoc nem nguyen len tren - cac Service goi client
 * nay se fallback ve cache CU (neu con, du qua TTL) de khong lam sap toan bo app kiosk chi vi
 * admin-portal tam thoi khong toi duoc.
 */
@Component
public class AdminPortalClient {

    private final RestTemplate restTemplate;
    private final AdminPortalProperties properties;
    private final AdminPortalHmacSigner signer;

    private final TtlSlot<List<AdminProductDto>> productsSlot = new TtlSlot<>();
    private final TtlSlot<List<AdminCategoryDto>> categoriesSlot = new TtlSlot<>();
    private final TtlSlot<List<AdminStoreDto>> storesSlot = new TtlSlot<>();
    private final TtlSlot<AdminLimitDto> limitSlot = new TtlSlot<>();

    public AdminPortalClient(RestTemplateBuilder builder, AdminPortalProperties properties,
                              AdminPortalHmacSigner signer) {
        this.properties = properties;
        this.signer = signer;
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .build();
    }

    public List<AdminProductDto> getProducts() {
        return productsSlot.get(ttl(), () -> callList("/api/products", new ParameterizedTypeReference<>() {
        }));
    }

    /** Bo qua TTL, doc tuoi tu admin-portal va cap nhat cache - dung cho vong phat hien doi gia SSE. */
    public List<AdminProductDto> refreshProducts() {
        return productsSlot.refresh(ttl(), () -> callList("/api/products", new ParameterizedTypeReference<>() {
        }));
    }

    /** Het han ngay moi cache (van giu ban cu lam du phong neu admin-portal loi) - goi khi admin vua sua. */
    public void invalidateAll() {
        productsSlot.expire();
        categoriesSlot.expire();
        storesSlot.expire();
        limitSlot.expire();
    }

    public List<AdminCategoryDto> getCategories() {
        return categoriesSlot.get(ttl(), () -> callList("/api/categories", new ParameterizedTypeReference<>() {
        }));
    }

    /** Bo qua TTL, doc tuoi danh muc - dung cho vong phat hien doi danh muc (SSE config-changed). */
    public List<AdminCategoryDto> refreshCategories() {
        return categoriesSlot.refresh(ttl(), () -> callList("/api/categories", new ParameterizedTypeReference<>() {
        }));
    }

    public List<AdminStoreDto> getStores() {
        return storesSlot.get(ttl(), () -> callList("/api/stores", new ParameterizedTypeReference<>() {
        }));
    }

    public AdminLimitDto getActiveLimit() {
        return limitSlot.get(ttl(), () -> callObject("/api/limits", new ParameterizedTypeReference<>() {
        }));
    }

    private Duration ttl() {
        return Duration.ofSeconds(properties.getCacheTtlSeconds());
    }

    private <T> List<T> callList(String path, ParameterizedTypeReference<AdminApiEnvelope<List<T>>> type) {
        AdminApiEnvelope<List<T>> body = call(path, type);
        return body.data() == null ? List.of() : body.data();
    }

    private <T> T callObject(String path, ParameterizedTypeReference<AdminApiEnvelope<T>> type) {
        return call(path, type).data();
    }

    private <T> T call(String path, ParameterizedTypeReference<T> type) {
        AdminPortalHmacSigner.Signed signed = signer.sign("GET", path, properties.getSecretKey());
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", properties.getApiKey());
        headers.set("X-Timestamp", signed.timestamp());
        headers.set("X-Nonce", signed.nonce());
        headers.set("X-Signature", signed.signature());
        ResponseEntity<T> resp;
        try {
            resp = restTemplate.exchange(properties.getBaseUrl() + path, HttpMethod.GET,
                    new HttpEntity<>(headers), type);
        } catch (RestClientException e) {
            throw new AdminPortalUnavailableException("Khong goi duoc admin-portal " + path, e);
        }
        T body = resp.getBody();
        if (body == null) {
            throw new AdminPortalUnavailableException("admin-portal tra body rong cho " + path, null);
        }
        return body;
    }

    /** 1 slot cache gia tri + thoi diem het han, khoa bang ReentrantLock khi refresh (tranh thundering herd). */
    private static final class TtlSlot<T> {
        private volatile T value;
        private volatile Instant expiresAt = Instant.EPOCH;
        private final ReentrantLock lock = new ReentrantLock();

        T get(Duration ttl, java.util.function.Supplier<T> loader) {
            Instant now = Instant.now();
            if (value != null && now.isBefore(expiresAt)) {
                return value;
            }
            return load(ttl, loader, false);
        }

        void expire() {
            expiresAt = Instant.EPOCH;
        }

        T refresh(Duration ttl, java.util.function.Supplier<T> loader) {
            return load(ttl, loader, true);
        }

        private T load(Duration ttl, java.util.function.Supplier<T> loader, boolean force) {
            lock.lock();
            try {
                if (!force && value != null && Instant.now().isBefore(expiresAt)) {
                    return value;
                }
                try {
                    T fresh = loader.get();
                    value = fresh;
                    expiresAt = Instant.now().plus(ttl);
                    return fresh;
                } catch (AdminPortalUnavailableException e) {
                    // admin-portal tam thoi khong toi duoc: tra ban CU (du het TTL) con hon la 500 ngay -
                    // chi nem loi khi CHUA TUNG co ban nao de cache (lan dau, khong co fallback).
                    if (value != null) {
                        return value;
                    }
                    throw e;
                }
            } finally {
                lock.unlock();
            }
        }
    }
}
