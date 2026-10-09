package vn.baotinmanhhai.kioskapi.catalog.dto;

import java.math.BigDecimal;
import java.util.List;

/** II.2.8 - toan bo truong cua ProductListItemDto them description. */
public record ProductDetailDto(
        String id,
        String name,
        String categoryId,
        String code,
        BigDecimal quantity,
        long price,
        Long buyPrice,
        String priceUpdatedAt,
        int remainingQuantity,
        String description,
        List<ProductImageDto> images,
        boolean invisible,
        String priceId,
        String serviceCode
) {
}
