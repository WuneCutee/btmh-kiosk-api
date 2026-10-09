package vn.baotinmanhhai.kioskapi.price.dto;

import java.math.BigDecimal;

public record PriceItemDto(
        String productId,
        String code,
        String name,
        BigDecimal quantity,
        long price,
        Long buyPrice,
        String priceTrend,
        String buyPriceTrend,
        String priceUpdatedAt,
        int order,
        int remainingQuantity
) {
}
