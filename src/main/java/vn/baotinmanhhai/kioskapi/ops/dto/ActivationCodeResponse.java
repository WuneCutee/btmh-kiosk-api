package vn.baotinmanhhai.kioskapi.ops.dto;

public record ActivationCodeResponse(
        String deviceCode,
        String activationCode,
        String expiresAt
) {
}
