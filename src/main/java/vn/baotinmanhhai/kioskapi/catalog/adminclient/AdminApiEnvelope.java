package vn.baotinmanhhai.kioskapi.catalog.adminclient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Khop dung {@code BankApiResponse<T>} (admin-portal) - {@code {code, success, data, errors}}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminApiEnvelope<T>(
        @JsonProperty("code") int code,
        @JsonProperty("success") boolean success,
        @JsonProperty("data") T data
) {
}
