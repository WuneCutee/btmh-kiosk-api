package vn.baotinmanhhai.kioskapi.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** II.2.1 POST /api/v1/kiosk/device-sessions */
@Getter
@Setter
public class DeviceSessionRequest {

    @NotBlank
    private String deviceCode;

    @NotNull
    private Long timestamp;

    @NotBlank
    private String nonce;

    @NotBlank
    private String signature;

    private String appVersion;

    private String osVersion;
}
