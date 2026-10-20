package vn.baotinmanhhai.kioskapi.catalog.dto;

/** II.2.10 - id so theo spec (stores.id / province.id / ward.id ben admin-portal). */
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
