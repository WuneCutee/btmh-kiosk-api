package vn.baotinmanhhai.kioskapi.common;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Tien ich thoi gian chung. Toan bo server dung gio Viet Nam (Asia/Ho_Chi_Minh, UTC+7, khong
 * doi theo DST vi Viet Nam khong ap dung gio mua he).
 */
public final class TimeUtil {

    public static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private TimeUtil() {
    }

    /**
     * Thoi diem server tra ve trong truong top-level "timestamp" cua moi response: gio Viet Nam,
     * KHONG kem offset, phan le giay 0-9 chu so tu nhien theo ISO_LOCAL_DATE_TIME.
     */
    public static String nowResponseTimestamp() {
        return LocalDateTime.now(VIETNAM_ZONE).toString();
    }

    /** Gio Viet Nam hien tai cho cac truong khac (co offset +07:00), dung ISO-8601. */
    public static ZonedDateTime nowVietnam() {
        return ZonedDateTime.now(VIETNAM_ZONE);
    }

    public static long nowEpochMillis() {
        return System.currentTimeMillis();
    }
}
