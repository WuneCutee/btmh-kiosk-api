package vn.baotinmanhhai.kioskapi.catalog.dto;

import java.math.BigDecimal;
import java.util.List;

/** II.2.6/II.2.7 - ban rut gon (khong co description). */
public record ProductListItemDto(
        String id,
        String name,
        String categoryId,
        String code,
        BigDecimal quantity,
        long price,
        Long buyPrice,
        String priceUpdatedAt,
        int remainingQuantity,
        List<ProductImageDto> images,
        boolean invisible,
        String priceId,
        String serviceCode
) {
}
