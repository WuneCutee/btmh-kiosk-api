package vn.baotinmanhhai.kioskapi.catalog.dto;

public record StoreDto(
        long id,
        String name,
        String address,
        long provinceId,
        Long wardId,
        Integer numberDaysReceive,
        String wardName,
        String receiveDate,
        String pickupHours
) {
}
