package vn.baotinmanhhai.kioskapi.catalog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** II.2.4 - banner dau danh sach san pham. */
@Entity
@Table(name = "kiosk_banner")
@Getter
@Setter
@NoArgsConstructor
public class BannerEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "image_path", length = 500, nullable = false)
    private String imagePath;

    @Column(name = "descriptions", length = 1000)
    private String descriptions;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
}
