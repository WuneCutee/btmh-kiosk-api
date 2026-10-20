package vn.baotinmanhhai.kioskapi.ops.dto;

import vn.baotinmanhhai.kioskapi.catalog.entity.BannerEntity;

/** Banner cho web admin (gom ca ban ghi dang tat). */
public record OpsBannerDto(
        String id,
        String code,
        String name,
        String imagePath,
        String descriptions,
        int priority,
        boolean enabled
) {
    public static OpsBannerDto from(BannerEntity b) {
        return new OpsBannerDto(b.getId(), b.getCode(), b.getName(), b.getImagePath(), b.getDescriptions(),
                b.getPriority(), b.isEnabled());
    }
}
