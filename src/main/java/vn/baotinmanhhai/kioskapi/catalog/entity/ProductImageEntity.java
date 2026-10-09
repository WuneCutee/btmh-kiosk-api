package vn.baotinmanhhai.kioskapi.catalog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "kiosk_product_image")
@Getter
@Setter
@NoArgsConstructor
public class ProductImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", length = 36, nullable = false)
    private String productId;

    @Column(name = "url", length = 500, nullable = false)
    private String url;

    @Column(name = "priority", nullable = false)
    private int priority;
}
