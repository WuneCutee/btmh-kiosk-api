package vn.baotinmanhhai.kioskapi.catalog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** II.2.6/II.2.7/II.2.8 - san pham kem gia. */
@Entity
@Table(name = "kiosk_product")
@Getter
@Setter
@NoArgsConstructor
public class ProductEntity {

    public static final String TREND_UP = "UP";
    public static final String TREND_DOWN = "DOWN";
    public static final String TREND_SAME = "SAME";

    @Id
    @Column(name = "id", length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "category_id", length = 36, nullable = false)
    private String categoryId;

    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "name", length = 500, nullable = false)
    private String name;

    @Column(name = "description", length = 4000)
    private String description;

    @Column(name = "quantity", precision = 10, scale = 2, nullable = false)
    private BigDecimal quantity;

    // Nullable: san pham "chua co gia" (chua duoc nguon gia day) se bi loc khoi mi API danh sach
    // theo dung quy tac "San pham chua co gia khong xuat hien" cua tai lieu.
    @Column(name = "price")
    private Long price;

    @Column(name = "buy_price")
    private Long buyPrice;

    @Column(name = "price_trend", length = 10, nullable = false)
    private String priceTrend = TREND_SAME;

    @Column(name = "buy_price_trend", length = 10, nullable = false)
    private String buyPriceTrend = TREND_SAME;

    @Column(name = "price_updated_at", nullable = false)
    private Instant priceUpdatedAt = Instant.now();

    @Column(name = "remaining_quantity", nullable = false)
    private int remainingQuantity;

    @Column(name = "price_id", length = 36, nullable = false)
    private String priceId = UUID.randomUUID().toString();

    @Column(name = "service_code", length = 50)
    private String serviceCode;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
