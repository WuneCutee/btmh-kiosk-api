package vn.baotinmanhhai.kioskapi.price.dto;

import java.util.List;

public record PriceEventDto(long version, List<PriceItemDto> items) {
}
