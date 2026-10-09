package vn.baotinmanhhai.kioskapi.device.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

/**
 * Chong phat lai: nonce da dung trong 10 phut gan nhat -> 401 code 103. CHI ghi sau khi chu ky
 * da duoc kiem thanh cong (xem thu tu kiem o II.5.2) de request gia khong lam hong budget nonce
 * cua may thuc. Don dep ban ghi cu qua 10 phut bang scheduled job (xem DeviceNonceCleanupJob).
 */
@Entity
@Table(name = "kiosk_device_nonce")
@Getter
@Setter
@NoArgsConstructor
public class DeviceNonceEntity {

    @EmbeddedId
    private Key id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public DeviceNonceEntity(String deviceCode, String nonce, Instant createdAt) {
        this.id = new Key(deviceCode, nonce);
        this.createdAt = createdAt;
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Key implements Serializable {
        @Column(name = "device_code", length = 50)
        private String deviceCode;

        @Column(name = "nonce", length = 32)
        private String nonce;

        public Key(String deviceCode, String nonce) {
            this.deviceCode = deviceCode;
            this.nonce = nonce;
        }
    }
}
