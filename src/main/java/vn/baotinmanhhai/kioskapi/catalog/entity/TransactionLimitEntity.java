package vn.baotinmanhhai.kioskapi.catalog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** II.2.11 - han muc giao dich, mot ban ghi duy nhat (id = 1). null -> khong co han muc. */
@Entity
@Table(name = "kiosk_transaction_limit")
@Getter
@Setter
@NoArgsConstructor
public class TransactionLimitEntity {

    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(name = "limit_quantity", precision = 10, scale = 2)
    private BigDecimal limitQuantity;
}
