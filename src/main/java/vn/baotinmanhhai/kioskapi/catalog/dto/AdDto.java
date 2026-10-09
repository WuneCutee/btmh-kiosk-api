package vn.baotinmanhhai.kioskapi.catalog.dto;

public record AdDto(
        String id,
        String name,
        String mediaType,
        String url,
        int order,
        String actionType,
        String productId,
        String categoryId,
        String websiteUrl,
        String checksum,
        long sizeBytes,
        String updatedAt,
        int displaySeconds
) {
}
