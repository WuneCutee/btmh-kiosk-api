package vn.baotinmanhhai.kioskapi.catalog.adminclient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/** Khop dung {@code ProductData} (admin-portal, GET /api/products) - chi doc field can dung. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminProductDto(
        @JsonProperty("product_id") String productId,
        @JsonProperty("product_code") String productCode,
        @JsonProperty("product_name") String productName,
        @JsonProperty("sell") String sell,
        @JsonProperty("buy") String buy,
        @JsonProperty("status") String status,
        @JsonProperty("category_code") String categoryCode,
        @JsonProperty("quantity") BigDecimal quantity,
        @JsonProperty("description") String description,
        @JsonProperty("images") List<Image> images,
        @JsonProperty("updated_at") String updatedAt,
        @JsonProperty("reserve") Integer reserve
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Image(@JsonProperty("url") String url, @JsonProperty("priority") Integer priority) {
    }
}
