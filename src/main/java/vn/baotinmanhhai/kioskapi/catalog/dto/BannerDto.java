package vn.baotinmanhhai.kioskapi.catalog.dto;

public record BannerDto(
        String id,
        String code,
        String name,
        String imagePath,
        String descriptions,
        int priority
) {
}
