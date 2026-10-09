package vn.baotinmanhhai.kioskapi.catalog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** II.2.5 - danh muc san pham (tab). */
@Entity
@Table(name = "kiosk_category")
@Getter
@Setter
@NoArgsConstructor
public class CategoryEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
}
