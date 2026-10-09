package vn.baotinmanhhai.kioskapi.catalog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

/**
 * Cau hinh chung cho kenh KIOSK: mot ban ghi duy nhat (id = 1), dung cho II.2.3/II.2.12 (gio mo
 * ban, hotline, idle timeout, nguong gia cu, minAppVersion) va co chuyen hoat dong/tam dung kenh
 * (ma loi 201). Khong co admin UI trong pham vi du an nay (xem README "Ngoai pham vi") - sua
 * truc tiep bang SQL hoac mot cong cu quan tri rieng trong trien khai thuc te.
 */
@Entity
@Table(name = "kiosk_channel_config")
@Getter
@Setter
@NoArgsConstructor
public class ChannelConfigEntity {

    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "enable_hkd", nullable = false)
    private boolean enableHkd = false;

    @Column(name = "hotline", length = 30, nullable = false)
    private String hotline = "1800.2209";

    @Column(name = "idle_timeout_seconds", nullable = false)
    private int idleTimeoutSeconds = 60;

    @Column(name = "stale_price_seconds", nullable = false)
    private int stalePriceSeconds = 300;

    @Column(name = "min_app_version", length = 20, nullable = false)
    private String minAppVersion = "1.0.0";

    @Column(name = "channel_paused", nullable = false)
    private boolean channelPaused = false;
}
