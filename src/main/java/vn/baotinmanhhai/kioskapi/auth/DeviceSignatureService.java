package vn.baotinmanhhai.kioskapi.auth;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Xac minh chu ky RSASSA-PSS-SHA256 dung CHINH XAC thong so chot trong muc II.5.2 cua tai lieu:
 * SHA-256, MGF1-SHA256, salt length = 32 byte, trailer field = 1 (chuan PKCS#1). Day la phan
 * security-critical nhat cua toan bo du an - xem KioskSignatureTestVectorTest cho test vector
 * chinh thuc trong tai lieu.
 *
 * <p>baseString: cac thanh phan noi bang dung MOT ky tu LF (0x0A), UTF-8, khong LF cuoi, khong
 * khoang trang thua - xay dung o DeviceAuthService/EnrollService/RotateKeyService, class nay chi
 * lo phan verify/parse khoa.
 */
@Service
public class DeviceSignatureService {

    private static final String SIGNATURE_ALGORITHM = "RSASSA-PSS";
    private static final int PSS_SALT_LENGTH = 32;
    private static final int PSS_TRAILER_FIELD = 1;

    /**
     * @param baseString  chuoi goc (chua ky, chua hash) - UTF-8
     * @param signatureB64 chu ky Base64 chuan
     * @param publicKeyB64 public key X.509 SubjectPublicKeyInfo DER, Base64 chuan
     * @return true neu chu ky hop le voi public key da cho
     */
    public boolean verify(String baseString, String signatureB64, String publicKeyB64) {
        try {
            PublicKey publicKey = decodePublicKey(publicKeyB64);
            byte[] signatureBytes = Base64.getDecoder().decode(signatureB64);
            byte[] message = baseString.getBytes(StandardCharsets.UTF_8);

            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.setParameter(new PSSParameterSpec(
                    "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSS_SALT_LENGTH, PSS_TRAILER_FIELD));
            signature.initVerify(publicKey);
            signature.update(message);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            // Moi loi dinh dang (base64 sai, khoa sai, chu ky sai do dai...) deu coi la chu ky
            // khong hop le (code 101), KHONG lo chi tiet loi.
            return false;
        }
    }

    public PublicKey decodePublicKey(String publicKeyB64) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] der = Base64.getDecoder().decode(publicKeyB64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    /** Xay baseString tu cac thanh phan, noi bang LF (0x0A) dung quy tac II.5.2. */
    public static String buildBaseString(String... parts) {
        return String.join("\n", parts);
    }
}
