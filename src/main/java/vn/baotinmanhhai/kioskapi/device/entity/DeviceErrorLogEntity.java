package vn.baotinmanhhai.kioskapi.device.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Luu loi tu may (II.2.13) de BTMH xem van hanh. Dedup theo (deviceCode, errorId). */
@Entity
@Table(name = "kiosk_device_error_log",
        uniqueConstraints = @UniqueConstraint(columnNames = {"device_code", "error_id"}))
@Getter
@Setter
@NoArgsConstructor
public class DeviceErrorLogEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "device_code", length = 50, nullable = false)
    private String deviceCode;

    @Column(name = "error_id", length = 36, nullable = false)
    private String errorId;

    @Column(name = "occurred_at", length = 40, nullable = false)
    private String occurredAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "type", length = 30, nullable = false)
    private String type;

    @Column(name = "message", length = 500, nullable = false)
    private String message;

    @Lob
    @Column(name = "context")
    private String context;
}
