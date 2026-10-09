package vn.baotinmanhhai.kioskapi.price.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Bo dem version chung cho snapshot/price (II.3), luu trong DB (mot ban ghi, id = 1) de SONG VOI
 * du lieu qua lan restart - tuong tu goi y "Redis INCR" trong tai lieu nhung thay bang mot bang
 * don gian vi du an nay khong dung Redis (quyet dinh single-instance, xem README).
 */
@Entity
@Table(name = "kiosk_price_version")
@Getter
@Setter
@NoArgsConstructor
public class PriceVersionEntity {

    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(name = "value", nullable = false)
    private long value = 0L;
}
