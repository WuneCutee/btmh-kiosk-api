package vn.baotinmanhhai.kioskapi.device.dto;

public record EnrollResponse(
        String deviceCode,
        String status,
        String activatedAt,
        long serverTime
) {
}
