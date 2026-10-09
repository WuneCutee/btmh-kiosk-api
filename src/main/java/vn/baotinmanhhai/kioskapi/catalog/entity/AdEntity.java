package vn.baotinmanhhai.kioskapi.catalog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/** II.2.2 - quang cao man cho. */
@Entity
@Table(name = "kiosk_ad")
@Getter
@Setter
@NoArgsConstructor
public class AdEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "media_type", length = 10, nullable = false)
    private String mediaType; // IMAGE | VIDEO

    @Column(name = "url", length = 500, nullable = false)
    private String url;

    @Column(name = "display_order", nullable = false)
    private int order;

    @Column(name = "action_type", length = 10, nullable = false)
    private String actionType; // NONE | PRODUCT | CATEGORY | WEBSITE

    @Column(name = "product_id", length = 36)
    private String productId;

    @Column(name = "category_id", length = 36)
    private String categoryId;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Column(name = "checksum", length = 64, nullable = false)
    private String checksum;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "display_seconds", nullable = false)
    private int displaySeconds;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
}
