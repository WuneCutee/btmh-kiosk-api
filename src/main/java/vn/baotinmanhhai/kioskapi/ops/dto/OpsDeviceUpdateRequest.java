package vn.baotinmanhhai.kioskapi.ops.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Doi cua hang gan voi may. */
public record OpsDeviceUpdateRequest(
        @NotNull @Min(1) Long storeId,
        @NotBlank @Size(max = 255) String storeName
) {
}
