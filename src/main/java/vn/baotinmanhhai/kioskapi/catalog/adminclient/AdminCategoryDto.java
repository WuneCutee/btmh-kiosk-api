package vn.baotinmanhhai.kioskapi.catalog.adminclient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Khop dung {@code CategoryData} (admin-portal, GET /api/categories). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminCategoryDto(
        @JsonProperty("category_code") String categoryCode,
        @JsonProperty("category_name") String categoryName,
        @JsonProperty("description") String description,
        @JsonProperty("priority") Integer priority
) {
}
