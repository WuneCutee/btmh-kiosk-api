package vn.baotinmanhhai.kioskapi.ops;

import org.junit.jupiter.api.Test;
import vn.baotinmanhhai.kioskapi.common.ApiException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/** Checksum/kich thuoc media quang cao: SHA-256 hex chu thuong, dung doc ngay khi vuot gioi han. */
class MediaInspectorTest {

    private final MediaInspector inspector = new MediaInspector();

    @Test
    void computesSha256AndSize() throws Exception {
        byte[] data = "abc".getBytes(StandardCharsets.US_ASCII);
        MediaInspector.MediaInfo info = inspector.inspect(new ByteArrayInputStream(data), "IMAGE");
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", info.checksum());
        assertEquals(3, info.sizeBytes());
    }

    @Test
    void imageOverFiveMegabytesIsRejected() {
        byte[] data = new byte[(int) MediaInspector.MAX_IMAGE_BYTES + 1];
        ApiException ex = assertThrows(ApiException.class,
                () -> inspector.inspect(new ByteArrayInputStream(data), "IMAGE"));
        assertEquals("400", ex.getCode());
    }

    @Test
    void imageExactlyAtLimitIsAccepted() throws Exception {
        byte[] data = new byte[(int) MediaInspector.MAX_IMAGE_BYTES];
        assertEquals(MediaInspector.MAX_IMAGE_BYTES,
                inspector.inspect(new ByteArrayInputStream(data), "IMAGE").sizeBytes());
    }

    @Test
    void videoAllowsMoreThanImageLimit() throws Exception {
        byte[] data = new byte[(int) MediaInspector.MAX_IMAGE_BYTES + 1];
        assertEquals(data.length, inspector.inspect(new ByteArrayInputStream(data), "VIDEO").sizeBytes());
    }

    @Test
    void stopsReadingOnceLimitExceeded() {
        CountingEndlessStream endless = new CountingEndlessStream();
        assertThrows(ApiException.class, () -> inspector.inspect(endless, "IMAGE"));
        assertTrue(endless.read <= MediaInspector.MAX_IMAGE_BYTES + 8192,
                "khong duoc doc tiep sau khi vuot gioi han");
    }

    @Test
    void emptyMediaIsRejected() {
        assertThrows(ApiException.class, () -> inspector.inspect(new ByteArrayInputStream(new byte[0]), "IMAGE"));
    }

    /** Stream vo han, dem so byte da bi doc. */
    private static final class CountingEndlessStream extends InputStream {
        long read;

        @Override
        public int read() {
            read++;
            return 0;
        }

        @Override
        public int read(byte[] b, int off, int len) {
            read += len;
            return len;
        }
    }
}
