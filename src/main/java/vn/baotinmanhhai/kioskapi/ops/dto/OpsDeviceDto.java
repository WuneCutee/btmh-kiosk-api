package vn.baotinmanhhai.kioskapi.ops.dto;

/** May kiosk cho web admin: trang thai + khoa ACTIVE hien tai + dang mo SSE hay khong. */
public record OpsDeviceDto(
        String deviceCode,
        long storeId,
        String storeName,
        String status,
        String appVersion,
        String osVersion,
        String deviceModel,
        String createdAt,
        String activatedAt,
        String lastRotatedAt,
        boolean forceKeyRotation,
        boolean online,
        String keyId,
        String publicKey
) {
}
