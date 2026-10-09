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
 * Danh sach khoa cua may: mot may co the co NHIEU ban ghi khoa qua thoi gian (ACTIVE/RETIRING/
 * RETIRED), mo hinh dung y tuong cua webview V1.3__kiosk_device_key.sql de xu ly chong lap 10
 * phut khi xoay khoa.
 */
@Entity
@Table(name = "kiosk_device_key")
@Getter
@Setter
@NoArgsConstructor
public class DeviceKeyEntity {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_RETIRING = "RETIRING";
    public static final String STATUS_RETIRED = "RETIRED";

    public static final String SOURCE_ENROLL = "ENROLL";
    public static final String SOURCE_ROTATE = "ROTATE";

    @Id
    @Column(name = "key_id", length = 50)
    private String keyId;

    @Column(name = "device_code", length = 50, nullable = false)
    private String deviceCode;

    @Column(name = "public_key", length = 600, nullable = false)
    private String publicKey;

    @Column(name = "status", length = 10, nullable = false)
    private String status;

    @Column(name = "source", length = 10, nullable = false)
    private String source;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "retiring_until")
    private Instant retiringUntil;

    @Column(name = "retired_at")
    private Instant retiredAt;

    public boolean isActive() {
        return STATUS_ACTIVE.equals(status);
    }
}
