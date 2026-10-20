package vn.baotinmanhhai.kioskapi.catalog.adminclient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Khop dung {@code StoreListResponse.StoreItem} (admin-portal, GET /api/stores). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminStoreDto(
        @JsonProperty("id") String id,
        @JsonProperty("internal_id") Long internalId,
        @JsonProperty("province_internal_id") Long provinceInternalId,
        @JsonProperty("ward_internal_id") Long wardInternalId,
        @JsonProperty("name") String name,
        @JsonProperty("address") String address,
        @JsonProperty("province_id") String provinceId,
        @JsonProperty("province_name") String provinceName,
        @JsonProperty("ward_id") String wardId,
        @JsonProperty("ward_name") String wardName,
        @JsonProperty("number_days_receive") Integer numberDaysReceive,
        @JsonProperty("description") String description
) {
}
