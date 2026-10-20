package vn.baotinmanhhai.kioskapi.catalog.adminclient;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.util.Base64;

/**
 * Ky request HMAC-SHA256 dung DUNG thuat toan cua PartnerAuthFilter/HmacSignatureVerifier phia
 * admin-portal (Backend/admin-portal/.../services/partner/HmacSignatureVerifier.java) - khong
 * dung chung code, chi port lai 1:1 vi kiosk-api khong share module voi admin-portal.
 *
 * <p>Base string = METHOD\nPATH\nQUERY(rong, kiosk-api khong dung query co dau &)\nTIMESTAMP\n
 * NONCE\nSHA256(BODY rong vi toan bo call la GET khong body).
 */
@Component
public class AdminPortalHmacSigner {

    private static final String HMAC_ALGO = "HmacSHA256";
    private static final String EMPTY_BODY_SHA256 =
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    public record Signed(String timestamp, String nonce, String signature) {
    }

    public Signed sign(String method, String path, String secretKey) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = randomNonce();
        String baseString = String.join("\n",
                method.toUpperCase(), path, "", timestamp, nonce, EMPTY_BODY_SHA256);
        String signature = hmac(baseString, secretKey);
        return new Signed(timestamp, nonce, signature);
    }

    private String hmac(String baseString, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] raw = mac.doFinal(baseString.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(raw);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Khong the tinh HMAC-SHA256", e);
        }
    }

    private String randomNonce() {
        byte[] b = new byte[16];
        new java.security.SecureRandom().nextBytes(b);
        StringBuilder sb = new StringBuilder();
        for (byte x : b) {
            sb.append(String.format("%02x", x));
        }
        return sb.toString();
    }

    /** Lo ra de test/doi chieu neu can - khong dung trong luong goi thuc. */
    public static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
