package vn.baotinmanhhai.kioskapi.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Cap va kiem accessToken JWT (II.5.3). iss = "kiosk-api" (dinh danh rieng cua du an nay, KHONG
 * dung "backend-webview" vi do la ten cua webview). Secret rieng, khong dung chung voi du an nao
 * khac.
 */
@Service
public class JwtService {

    public static final String ISSUER = "kiosk-api";
    public static final String TOKEN_TYPE = "DEVICE";
    public static final String PARTNER_CODE = "KIOSK";
    public static final long EXPIRES_IN_SECONDS = 3600L;
    private static final long CLOCK_SKEW_LEEWAY_SECONDS = 60L;

    private final SecretKey key;

    public JwtService(@Value("${kiosk.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public record IssuedToken(String token, String jti, long iat, long exp) {
    }

    public IssuedToken issue(String deviceCode, long storeId, String keyId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(EXPIRES_IN_SECONDS);
        String jti = UUID.randomUUID().toString();
        String token = Jwts.builder()
                .issuer(ISSUER)
                .subject(deviceCode)
                .claim("tokenType", TOKEN_TYPE)
                .claim("partnerCode", PARTNER_CODE)
                .claim("storeId", storeId)
                .claim("keyId", keyId)
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
        return new IssuedToken(token, jti, now.getEpochSecond(), exp.getEpochSecond());
    }

    /** Ket qua giai ma thanh cong: cac claim can dung cho kiem tra tiep (sub, storeId, keyId...). */
    public record VerifiedToken(String deviceCode, long storeId, String keyId, String jti) {
    }

    /**
     * @return VerifiedToken neu hop le, hoac empty neu bat ky dieu kien nao sai (chu ky, exp,
     * iss, tokenType, partnerCode). KHONG throw ra ngoai cho cac loi JWT thong thuong - goi noi
     * quyet dinh map sang 401.
     */
    public VerifiedToken verify(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .clockSkewSeconds(CLOCK_SKEW_LEEWAY_SECONDS)
                    .requireIssuer(ISSUER)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenType = claims.get("tokenType", String.class);
            String partnerCode = claims.get("partnerCode", String.class);
            if (!TOKEN_TYPE.equals(tokenType) || !PARTNER_CODE.equals(partnerCode)) {
                return null;
            }
            String sub = claims.getSubject();
            Number storeIdNum = claims.get("storeId", Number.class);
            String keyId = claims.get("keyId", String.class);
            if (sub == null || storeIdNum == null || keyId == null) {
                return null;
            }
            return new VerifiedToken(sub, storeIdNum.longValue(), keyId, claims.getId());
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
