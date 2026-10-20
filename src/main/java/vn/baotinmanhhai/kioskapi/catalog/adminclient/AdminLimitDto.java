package vn.baotinmanhhai.kioskapi.catalog.adminclient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/** Khop dung {@code LimitData} (admin-portal, GET /api/limits). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminLimitDto(@JsonProperty("limit_quantity") BigDecimal limitQuantity) {
}
