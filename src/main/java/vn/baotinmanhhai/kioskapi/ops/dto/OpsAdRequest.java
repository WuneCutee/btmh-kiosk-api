package vn.baotinmanhhai.kioskapi.ops.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Tao/sua quang cao. mediaType/actionType/url va dich theo actionType kiem them o OpsAdService. */
public record OpsAdRequest(
        @NotBlank @Size(max = 50) String name,
        @NotBlank String mediaType,
        @NotBlank @Size(max = 500) String url,
        @NotNull @Min(1) Integer order,
        @NotBlank String actionType,
        @Size(max = 36) String productId,
        @Size(max = 36) String categoryId,
        @Size(max = 500) String websiteUrl,
        @NotNull @Min(1) @Max(300) Integer displaySeconds,
        Boolean enabled
) {
}
