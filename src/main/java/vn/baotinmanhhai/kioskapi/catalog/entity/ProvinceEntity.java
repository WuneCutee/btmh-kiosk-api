package vn.baotinmanhhai.kioskapi.catalog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** II.2.9 - tinh/thanh co cua hang. */
@Entity
@Table(name = "kiosk_province")
@Getter
@Setter
@NoArgsConstructor
public class ProvinceEntity {

    @Id
    private Long id;

    @Column(name = "code", length = 10, nullable = false)
    private String code;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
}
