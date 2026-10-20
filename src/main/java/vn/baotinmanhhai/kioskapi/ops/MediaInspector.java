package vn.baotinmanhhai.kioskapi.ops;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import vn.baotinmanhhai.kioskapi.common.ApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.OptionalLong;

/**
 * Tai file media quang cao tu URL de tinh checksum (SHA-256 hex chu thuong) va kich thuoc - App dung
 * 2 gia tri nay de cache/kiem file (II.2.2). Doc dang stream, dung ngay khi vuot gioi han (anh 5 MB,
 * video 50 MB), khong giu ca file trong bo nho.
 */
@Component
public class MediaInspector {

    public static final String MEDIA_IMAGE = "IMAGE";
    public static final String MEDIA_VIDEO = "VIDEO";
    public static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    public static final long MAX_VIDEO_BYTES = 50L * 1024 * 1024;

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final HttpClient httpClient;

    @Autowired
    public MediaInspector() {
        this(HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    /** Cho test: tiem HttpClient gia. */
    MediaInspector(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public record MediaInfo(String checksum, long sizeBytes) {
    }

    public static long maxBytes(String mediaType) {
        return MEDIA_VIDEO.equals(mediaType) ? MAX_VIDEO_BYTES : MAX_IMAGE_BYTES;
    }

    /** Tai url va tinh checksum/kich thuoc. Moi loi (mang, HTTP khong 2xx, qua lon, rong) -> 400. */
    public MediaInfo inspect(String url, String mediaType) {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder(URI.create(url)).timeout(REQUEST_TIMEOUT).GET().build();
        } catch (IllegalArgumentException e) {
            throw badRequest("url khong hop le: " + url);
        }
        HttpResponse<InputStream> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (IOException e) {
            throw badRequest("Khong tai duoc media tu url (loi ket noi/timeout)");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw badRequest("Tai media bi gian doan");
        }
        try (InputStream body = response.body()) {
            int status = response.statusCode();
            if (status < 200 || status > 299) {
                throw badRequest("Tai media that bai, HTTP " + status);
            }
            OptionalLong declared = response.headers().firstValueAsLong("Content-Length");
            if (declared.isPresent() && declared.getAsLong() > maxBytes(mediaType)) {
                throw tooLarge(mediaType);
            }
            return inspect(body, mediaType);
        } catch (IOException e) {
            throw badRequest("Khong doc duoc media tu url (loi ket noi/timeout)");
        }
    }

    /** Doc stream, tinh SHA-256 + so byte; vuot gioi han theo mediaType -> 400 ngay, khong doc tiep. */
    public MediaInfo inspect(InputStream in, String mediaType) throws IOException {
        long max = maxBytes(mediaType);
        MessageDigest digest = sha256();
        byte[] buffer = new byte[8192];
        long total = 0;
        int n;
        while ((n = in.read(buffer)) != -1) {
            total += n;
            if (total > max) {
                throw tooLarge(mediaType);
            }
            digest.update(buffer, 0, n);
        }
        if (total == 0) {
            throw badRequest("File media rong");
        }
        return new MediaInfo(HexFormat.of().formatHex(digest.digest()), total);
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 khong kha dung", e);
        }
    }

    private static ApiException tooLarge(String mediaType) {
        return badRequest(MEDIA_VIDEO.equals(mediaType)
                ? "Video vuot qua 50 MB"
                : "Anh vuot qua 5 MB");
    }

    private static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "400", message);
    }
}
