package vn.baotinmanhhai.kioskapi.ops.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cau hinh kenh KIOSK - dung chung cho GET (tra ve) va PUT (gui len). openTime/closeTime "HH:mm",
 * ca hai cung null hoac cung co (kiem o OpsChannelConfigService). enableHkd khong mo ra o day.
 */
public record OpsChannelConfigDto(
        String openTime,
        String closeTime,
        @NotBlank @Size(max = 30) String hotline,
        @NotNull @Min(15) @Max(3600) Integer idleTimeoutSeconds,
        @NotNull @Min(30) @Max(3600) Integer stalePriceSeconds,
        @NotBlank @Size(max = 20) @Pattern(regexp = "\\d+\\.\\d+\\.\\d+") String minAppVersion,
        @NotNull Boolean channelPaused
) {
}
