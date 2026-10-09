package vn.baotinmanhhai.kioskapi.catalog.dto;

/** II.2.12. */
public record BootstrapDto(
        String openTime,
        String closeTime,
        String hotline,
        int idleTimeoutSeconds,
        int stalePriceSeconds,
        String minAppVersion,
        long serverTime,
        long storeId,
        String storeName
) {
}
