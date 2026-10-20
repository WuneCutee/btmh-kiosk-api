package vn.baotinmanhhai.kioskapi.ops.dto;

import vn.baotinmanhhai.kioskapi.catalog.entity.AdEntity;
import vn.baotinmanhhai.kioskapi.common.TimeUtil;

import java.time.format.DateTimeFormatter;

/** Quang cao cho web admin (gom ca ban ghi dang tat). */
public record OpsAdDto(
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
        int displaySeconds,
        boolean enabled,
        String updatedAt
) {
    public static OpsAdDto from(AdEntity a) {
        return new OpsAdDto(a.getId(), a.getName(), a.getMediaType(), a.getUrl(), a.getOrder(), a.getActionType(),
                a.getProductId(), a.getCategoryId(), a.getWebsiteUrl(), a.getChecksum(), a.getSizeBytes(),
                a.getDisplaySeconds(), a.isEnabled(),
                a.getUpdatedAt() == null ? null
                        : a.getUpdatedAt().atZone(TimeUtil.VIETNAM_ZONE).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }
}
