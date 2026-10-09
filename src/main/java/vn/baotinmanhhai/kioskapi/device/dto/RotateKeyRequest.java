package vn.baotinmanhhai.kioskapi.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** II.2.15 POST /api/v1/kiosk/devices/rotate-key */
@Getter
@Setter
public class RotateKeyRequest {

    @NotBlank
    private String deviceCode;

    @NotNull
    private Long timestamp;

    @NotBlank
    private String nonce;

    @NotBlank
    private String newPublicKey;

    @NotBlank
    private String signature;

    @NotBlank
    private String newKeySignature;
}
