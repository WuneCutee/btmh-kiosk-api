package vn.baotinmanhhai.kioskapi.device.dto;

public record DeviceSessionResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String expiresAt,
        long serverTime,
        long storeId,
        String storeName,
        String minAppVersion,
        boolean keyRotationRequired
) {
}
