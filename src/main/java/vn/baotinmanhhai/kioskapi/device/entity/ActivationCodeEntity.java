package vn.baotinmanhhai.kioskapi.device.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "kiosk_activation_code")
@Getter
@Setter
@NoArgsConstructor
public class ActivationCodeEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "device_code", length = 50, nullable = false)
    private String deviceCode;

    @Column(name = "code_hash", length = 64, nullable = false)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
