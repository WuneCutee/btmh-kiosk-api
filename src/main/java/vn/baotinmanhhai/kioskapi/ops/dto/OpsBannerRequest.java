package vn.baotinmanhhai.kioskapi.ops.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Tao/sua banner. imagePath phai la http/https (kiem o OpsBannerController). */
public record OpsBannerRequest(
        @Size(max = 50) String code,
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 500) String imagePath,
        @Size(max = 1000) String descriptions,
        @NotNull @Min(1) Integer priority,
        Boolean enabled
) {
}
