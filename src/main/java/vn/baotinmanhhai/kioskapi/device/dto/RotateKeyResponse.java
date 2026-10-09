package vn.baotinmanhhai.kioskapi.device.dto;

public record RotateKeyResponse(
        String deviceCode,
        String overlapUntil,
        long serverTime
) {
}
