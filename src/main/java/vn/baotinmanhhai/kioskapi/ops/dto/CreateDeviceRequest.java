package vn.baotinmanhhai.kioskapi.ops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDeviceRequest {

    @NotBlank
    private String deviceCode;

    @NotNull
    private Long storeId;

    @NotBlank
    private String storeName;
}
