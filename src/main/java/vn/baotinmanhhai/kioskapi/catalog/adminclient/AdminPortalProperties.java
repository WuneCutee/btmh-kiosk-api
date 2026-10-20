package vn.baotinmanhhai.kioskapi.catalog.adminclient;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Cau hinh goi sang admin-portal (HMAC partner API, xem PartnerAuthFilter phia admin-portal).
 * kiosk-api la nguon doc (read-only client) - admin-portal la system-of-record cho san pham/
 * nhom/cua hang/han muc (quyet dinh 15/10/2026, xem README muc "Nguon catalog").
 */
@Configuration
@ConfigurationProperties(prefix = "admin-portal")
public class AdminPortalProperties {

    private String baseUrl = "http://localhost:8386";
    private String apiKey;
    private String secretKey;
    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 5000;
    /** TTL cache trong-tien-trinh cho 4 danh muc (san pham/nhom/cua hang/han muc) - khong Redis. */
    private int cacheTtlSeconds = 30;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public int getCacheTtlSeconds() {
        return cacheTtlSeconds;
    }

    public void setCacheTtlSeconds(int cacheTtlSeconds) {
        this.cacheTtlSeconds = cacheTtlSeconds;
    }
}
