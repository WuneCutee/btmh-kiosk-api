package vn.baotinmanhhai.kioskapi.catalog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** II.2.10 - cua hang nhan hang theo tinh/thanh. */
@Entity
@Table(name = "kiosk_store")
@Getter
@Setter
@NoArgsConstructor
public class StoreEntity {

    @Id
    private Long id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "address", length = 500, nullable = false)
    private String address;

    @Column(name = "province_id", nullable = false)
    private Long provinceId;

    @Column(name = "ward_id")
    private Long wardId;

    @Column(name = "ward_name", length = 255)
    private String wardName;

    @Column(name = "number_days_receive")
    private Integer numberDaysReceive;

    @Column(name = "pickup_hours", length = 50, nullable = false)
    private String pickupHours;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
}
