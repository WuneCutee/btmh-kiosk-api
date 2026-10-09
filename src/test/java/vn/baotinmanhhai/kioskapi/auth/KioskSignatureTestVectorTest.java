package vn.baotinmanhhai.kioskapi.auth;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.PSSParameterSpec;
import java.util.Base64;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test vector CHINH THUC tu muc II.5.2 cua tai lieu tich hop ("Test vector"). Day la bang kiem
 * tra quan trong nhat cua toan bo du an: chung minh DeviceSignatureService verify dung CHINH XAC
 * thuat toan RSASSA-PSS-SHA256 (salt 32 byte, trailer field 1, MGF1-SHA256) ma App Kiosk se dung.
 *
 * <p>Khoa trong test nay CHI dung de kiem thu phan mem, KHONG cap cho may thuc (dung nguyen van
 * nhu tai lieu ghi).
 */
class KioskSignatureTestVectorTest {

    private static final String DEVICE_CODE = "KIOSK-TT-01";
    private static final String TIMESTAMP = "1789614000000";
    private static final String NONCE_II21 = "26e07997327702cf";

    private static final String PUBLIC_KEY_B64 =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApxkvKWEWYFVZTVK29hatpcNwrJT12UjC2zHkFxoVze4+Rohr7IlQV4aVxFpk7f2XYQ9z9pLCFcLnX5Nxb5s6bazLaEtnyiVzV0+pyTmiXwQMXYeffPoOigwGRk9kVWKYL5TqHKlrhYpszbA0TvPEdyFjXCcZ2ZcNrrW/4tIfH3WwSp1WV0AmsW+hPQ02c5ZAwNR/9u6tbAlJAqu+JG1JVClTftwT7inLADYHSbqKdOXvRkLgn9WfNkH2nxuNXb0L3zVmpHwgPOnP9ed0zDHzmffWuzSTYnPKGXzOmmgOb1F97m9HzaEcGpejbojDeoTL9rvooEFx7e/zAmg4mjtHxQIDAQAB";

    private static final String PRIVATE_KEY_B64 =
            "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCnGS8pYRZgVVlNUrb2Fq2lw3CslPXZSMLbMeQXGhXN7j5GiGvsiVBXhpXEWmTt/ZdhD3P2ksIVwudfk3FvmzptrMtoS2fKJXNXT6nJOaJfBAxdh598+g6KDAZGT2RVYpgvlOocqWuFimzNsDRO88R3IWNcJxnZlw2utb/i0h8fdbBKnVZXQCaxb6E9DTZzlkDA1H/27q1sCUkCq74kbUlUKVN+3BPuKcsANgdJuop05e9GQuCf1Z82QfafG41dvQvfNWakfCA86c/153TMMfOZ99a7NJNic8oZfM6aaA5vUX3ub0fNoRwal6NuiMN6hMv2u+igQXHt7/MCaDiaO0fFAgMBAAECggEAAM8TJp7g8hnvqvtnxdSxxf/mc3tsHj9nLBL3gPIgIWVSGVrPNO+sOyQ8ZN0wKGPGIBLoU4z90gelwZVv4uOsOnwv5uMYj0azonxQ/Neh1NZq7ef6ams2d04cGKqJOI2X4YEVygo66kRThwJfmnM9I/t+JOPOkPpVd19LXMB6JJeWAFqvCL/ZxWVhWH/KIjVWH40VSZpdsfaZobGsEOT8YmXgTEPQzBSJxlkTzvm0nqi1ddV8ye+el7duIFBU2H+Y7YGCr0YebM8vXlcgUe4kyeIopsm8W1mk7xCU6Q1LaZes72szj5xoO5lWv6G8HUlssruAQNcg3TUaTUOcYz/M4QKBgQDUBaxX6T68neVAQcSW3Sl7IGi1hyFfr7XetR2G0W7Dj8dGBL1fKC6JDWB0IhGXr/3r2YEF9q7p8IglzJjuWuJpB4y8EezBwMtnPun7qU61FuOFH4LNidOo7iNIg9klQoDyBARsCkQR6HIxjKJJx1pBzPbAuVTdnXPnWvAWmoIx5QKBgQDJwhGQIqZqe4NTKMQtz73NlcOs1VoTKz4oGUNdZepTbW1ktsAK/2gb6ax/JAR1Zm55M+kX2Qk7WxdYZGXYpH0XXHMNmJGhHWq4+/y9Ey6EFpbP77TiOKZbGZWpFPsRnOhUKT+Ah3lomi7Y/fRl6ZpP7+niB0wJQCu/gZ0hGmbgYQKBgGCEfKJAcejXUCdiSSYMY/fDFyeb7ltq17YcaOaYrH2mFCcRvUt2NUj6p+/2GmAdI9Cb4Rpf49xnPPNIIk5PXUZGw5BR8J97Lp0/jLCdsFDLrKNLZvY+SfZbAgDe1JkbnyuNHJ46RiaI1IMt7Cr9716I+cNeGv4P0VV+llET4TTpAoGAHf6C++ma0fPAx0klOSZMMlcUewpQ1M618+IlmKoBFxwKilF85WWpBZVMtq4VF5r9y3kDKy9x5ugY5WQvm/CAREvTMu8+nzQvXIbhwP4sCSkOjKU3P/ZqDNsV28gptymbhH+JWENLslhRpKLEha4f6chh/d9V1345wsOzJ2u0ZaECgYAg7Z+UtTSmArDbyojMsAa+D2V99q4azYlrTIr3coOl3fw8U6JssUAnHCF6jBiWqDQpgyZRHhSl55WXi+kOIr5IPX2YgL9ICBkCMl8IJYqvrxphXBsTXKhOW4NCKOrXkOMRe8UdFDY2TJY4MlKUjA24EkOZuEcgKkeLI2IQ173mRg==";

    private static final String SIGNATURE_II21_B64 =
            "F9TW12tZn2onm4Fi9b7YIDXR3r5DV5YH6u+Y2Rbko06hL9YY74bo4hX+balA6n8yjeem+cvEGnwDT+RvZlp7fxeUd2Z79mvqudG3MrBtXk7auZLYeVHB3p6zlG5Eby1MJc9RvZoRn4GXisqFqt3CqN9RdlzNcVjGcLyTZf5ud2SDH7QIMPT4iCP+k25IY017qXYwwFyY4duBvLcLoE7TvzKgprXc9GhqPcjLby48uedO+oeb/2RptBcE6eqzMUZjbkKAtFEm1lreD6ED3QhZhrdS4Nxy3pnKgkGqxxHv4C+KdRv2k5q1S1D0+mtEqa1d6tPVteKcSPlrf5eiWoluyQ==";

    private static final String EXPECTED_SHA256_II21 =
            "82abfd1c787c476c6977bf8668e7a051668e1b124b22ea5e97c3f3b1951e1aec".substring(0, 64);

    private static final String ACTIVATION_CODE = "7K2M9QXA4T8P";
    private static final String NONCE_II214 = "9c41e0d27a5b3f18";

    private static final String SIGNATURE_II214_B64 =
            "VPhqBhcdTlCxbTDP4xzSfK8oPaRJDzckkq3m2SWNV8dhX16LDNvnSDERSjwmyZWn7vd5LYkE6frSQhiP6O4VlfyouC+oHeAmlViDYVGVqxfxCVcnXiRRYY1ndsI2o+rWjXFGhnMYq4F+UVRpFGS9EJbvcx58S55wBTyTjtKlSVUq9wasjBHUgCOPwDdoSHpfBKsMnPG3tK6sQa9tI9uTZIAv5um+hvYZDx14nnSz5uGuF1WXBKwM/eQganDYQWjn0A2ODaVPb8FaNaa03eDjc2HSdt0vBWSKCcAF0HOptgEUDm10c9fpVLSH7tBBF1hqC2CHwYtGTEHF0vmayXazZg==";

    private static final String EXPECTED_SHA256_II214 =
            "afae4cf1ab69857af5802bc816ca23905020510c3e3195804f9010df6a1a1190".substring(0, 64);

    private final DeviceSignatureService signatureService = new DeviceSignatureService();

    @Test
    void baseStringII21MatchesExpectedSha256() throws Exception {
        String baseString = DeviceSignatureService.buildBaseString(DEVICE_CODE, TIMESTAMP, NONCE_II21);
        assertEqualsHex(EXPECTED_SHA256_II21, sha256Hex(baseString));
    }

    @Test
    void verifiesOfficialSignatureII21WithPublicKey() {
        String baseString = DeviceSignatureService.buildBaseString(DEVICE_CODE, TIMESTAMP, NONCE_II21);
        boolean valid = signatureService.verify(baseString, SIGNATURE_II21_B64, PUBLIC_KEY_B64);
        assertTrue(valid, "Chu ky II.2.1 trong test vector phai hop le voi public key da cho");
    }

    @Test
    void selfSignedSignatureWithTestPrivateKeyAlsoVerifies() throws Exception {
        String baseString = DeviceSignatureService.buildBaseString(DEVICE_CODE, TIMESTAMP, NONCE_II21);
        String freshSignature = signWithPrivateKey(baseString, PRIVATE_KEY_B64);
        boolean valid = signatureService.verify(baseString, freshSignature, PUBLIC_KEY_B64);
        assertTrue(valid, "Chu ky tu ky bang private key test vector phai verify duoc bang public key tuong ung");
    }

    @Test
    void baseStringII214MatchesExpectedSha256() throws Exception {
        String baseString = DeviceSignatureService.buildBaseString(
                DEVICE_CODE, TIMESTAMP, NONCE_II214, ACTIVATION_CODE, PUBLIC_KEY_B64);
        assertEqualsHex(EXPECTED_SHA256_II214, sha256Hex(baseString));
    }

    @Test
    void verifiesOfficialSignatureII214WithPublicKey() {
        String baseString = DeviceSignatureService.buildBaseString(
                DEVICE_CODE, TIMESTAMP, NONCE_II214, ACTIVATION_CODE, PUBLIC_KEY_B64);
        boolean valid = signatureService.verify(baseString, SIGNATURE_II214_B64, PUBLIC_KEY_B64);
        assertTrue(valid, "Chu ky II.2.14 trong test vector phai hop le voi public key da cho");
    }

    @Test
    void tamperedBaseStringFailsVerification() {
        String tampered = DeviceSignatureService.buildBaseString(DEVICE_CODE, TIMESTAMP, "0000000000000000");
        boolean valid = signatureService.verify(tampered, SIGNATURE_II21_B64, PUBLIC_KEY_B64);
        assertFalse(valid, "Chu ky khong duoc verify dung tren baseString bi sua doi");
    }

    @Test
    void wrongPublicKeyFailsVerification() {
        String baseString = DeviceSignatureService.buildBaseString(DEVICE_CODE, TIMESTAMP, NONCE_II21);
        // Lay nham public key cua II.2.14's ... (chinh no, nhung thu voi chu ky sai cua api khac)
        boolean valid = signatureService.verify(baseString, SIGNATURE_II214_B64, PUBLIC_KEY_B64);
        assertFalse(valid, "Chu ky cua mot baseString khac khong duoc verify thanh cong tren baseString nay");
    }

    private static void assertEqualsHex(String expected, String actual) {
        assertTrue(expected.equalsIgnoreCase(actual), "Expected " + expected + " but was " + actual);
    }

    private static String sha256Hex(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }

    private static String signWithPrivateKey(String baseString, String privateKeyB64) throws Exception {
        byte[] der = Base64.getDecoder().decode(privateKeyB64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(spec);

        java.security.Signature signature = java.security.Signature.getInstance("RSASSA-PSS");
        signature.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
        signature.initSign(privateKey);
        signature.update(baseString.getBytes(StandardCharsets.UTF_8));
        byte[] sig = signature.sign();
        return Base64.getEncoder().encodeToString(sig);
    }
}
