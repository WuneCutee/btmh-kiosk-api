package vn.baotinmanhhai.kioskapi.catalog.dto;

/** II.2.9 - {@code id} so (province.id ben admin-portal), {@code code} = ma hanh chinh VN (vd "01"). */
public record ProvinceDto(long id, String code, String name, long storeCount) {
}
