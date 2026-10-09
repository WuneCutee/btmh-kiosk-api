package vn.baotinmanhhai.kioskapi.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** II.2.14 POST /api/v1/kiosk/devices/enroll */
@Getter
@Setter
public class EnrollRequest {

    @NotBlank
    private String deviceCode;

    @NotNull
    private Long timestamp;

    @NotBlank
    private String nonce;

    @NotBlank
    private String activationCode;

    @NotBlank
    private String publicKey;

    @NotBlank
    private String signature;

    private String appVersion;
    private String osVersion;
    private String deviceModel;
}
