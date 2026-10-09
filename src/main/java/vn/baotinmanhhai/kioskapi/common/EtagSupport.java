package vn.baotinmanhhai.kioskapi.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Tinh ETag noi dung (strong ETag, SHA-256 hex cua JSON da serialize) cho cac API co "Ho tro
 * ETag/If-None-Match" trong tai lieu (II.2.2 ads/playlist, II.2.12 bootstrap). Dung noi dung thuc
 * te thay vi dua vao cot updated_at cua entity: dung bat ke entity co bump updated_at day du hay
 * khong, chi phi khong dang ke voi kich thuoc du lieu cua 2 API nay (playlist/bootstrap config).
 */
@Component
public class EtagSupport {

    private final ObjectMapper objectMapper;

    public EtagSupport(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String hashOf(Object data) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(data);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(json);
            return "\"" + HexFormat.of().formatHex(hash) + "\"";
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 khong kha dung", e);
        } catch (Exception e) {
            // Loi serialize (khong nen xay ra voi DTO thuan) - tra ETag hang so de khong chan response
            // chinh, chi mat tac dung cache chu khong gay loi cho client.
            return "\"etag-unavailable\"";
        }
    }
}
