package vn.baotinmanhhai.kioskapi.device.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * May kiosk. Thiet ke dua theo schema da chung minh dung cua webview
 * (V1.2__kiosk_device_activation_rsa.sql) nhung la bang/entity RIENG cua kiosk-api, khong dung
 * chung database voi webview/kiosk-admin.
 */
@Entity
@Table(name = "kiosk_device")
@Getter
@Setter
@NoArgsConstructor
public class DeviceEntity {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_REVOKED = "REVOKED";

    @Id
    @Column(name = "device_code", length = 50)
    private String deviceCode;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "store_name", length = 255)
    private String storeName;

    @Column(name = "status", length = 10, nullable = false)
    private String status;

    @Column(name = "app_version", length = 20)
    private String appVersion;

    @Column(name = "os_version", length = 20)
    private String osVersion;

    @Column(name = "device_model", length = 100)
    private String deviceModel;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "last_rotated_at")
    private Instant lastRotatedAt;

    @Column(name = "force_key_rotation", nullable = false)
    private boolean forceKeyRotation;

    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    public boolean isActive() {
        return STATUS_ACTIVE.equals(status);
    }

    public boolean isRevoked() {
        return STATUS_REVOKED.equals(status);
    }
}
