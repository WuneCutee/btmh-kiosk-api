package vn.baotinmanhhai.kioskapi.device.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Trien khai lai (khong import cheo) ham sha256Hex giong chinh xac
 * kiosk-admin/src/main/java/vn/baotinmanhhai/kioskadmin/util/HashUtil.java de dam bao hanh vi
 * hash ma kich hoat nhat quan giua cac he thong BTMH. KHONG doi thuat toan nay.
 */
public final class HashUtil {

    private HashUtil() {
    }

    public static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** So sanh constant-time theo yeu cau spec ("so sanh constant-time" voi ma kich hoat). */
    public static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
