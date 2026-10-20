package vn.baotinmanhhai.kioskapi.price.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import vn.baotinmanhhai.kioskapi.catalog.adminclient.AdminCategoryDto;
import vn.baotinmanhhai.kioskapi.catalog.adminclient.AdminPortalClient;
import vn.baotinmanhhai.kioskapi.catalog.service.ProductService;
import vn.baotinmanhhai.kioskapi.price.repo.PriceVersionRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * (1) Dong SSE tren ket noi da hong khong duoc nem loi (truoc day complete() nem
 * AsyncRequestNotUsableException lam revoke/connect tra 500). (2) Danh muc doi -> config-changed.
 */
class PriceStreamServiceSseTest {

    private AdminPortalClient adminPortalClient;
    private PriceStreamService service;

    @BeforeEach
    void setUp() {
        adminPortalClient = mock(AdminPortalClient.class);
        ProductService productService = mock(ProductService.class);
        when(productService.allEnabledPricedInListOrder()).thenReturn(List.of());
        service = new PriceStreamService(adminPortalClient, productService, mock(PriceVersionRepository.class));
    }

    @Test
    void revokeDeviceSwallowsBrokenConnection() {
        service.connect("K1", new BrokenEmitter());
        assertTrue(service.connectedDeviceCodes().contains("K1"));
        assertDoesNotThrow(() -> service.revokeDevice("K1"));
        assertFalse(service.connectedDeviceCodes().contains("K1"));
    }

    @Test
    void revokeUnknownDeviceIsNoop() {
        assertDoesNotThrow(() -> service.revokeDevice("NOT-CONNECTED"));
    }

    @Test
    void replacingBrokenConnectionDoesNotThrow() {
        service.connect("K1", new BrokenEmitter());
        RecordingEmitter second = new RecordingEmitter();
        assertDoesNotThrow(() -> service.connect("K1", second));
        assertEquals(1, service.connectedDeviceCodes().size());
    }

    @Test
    void categoryChangeSendsConfigChangedAfterBaseline() {
        RecordingEmitter emitter = new RecordingEmitter();
        service.connect("K1", emitter);
        when(adminPortalClient.refreshCategories())
                .thenReturn(List.of(cat("NHAN", "Nhan", 1), cat("LAC", "Lac", 2)))
                .thenReturn(List.of(cat("LAC", "Lac", 2), cat("NHAN", "Nhan", 1)))
                .thenReturn(List.of(cat("NHAN", "Nhan vang", 1), cat("LAC", "Lac", 2)));

        service.pollAdminPortal(); // lan dau: chi lay moc
        assertFalse(emitter.events.contains("config-changed"));
        service.pollAdminPortal(); // chi doi thu tu tra ve, noi dung nhu cu
        assertFalse(emitter.events.contains("config-changed"));
        service.pollAdminPortal(); // doi ten
        assertEquals(1, emitter.events.stream().filter("config-changed"::equals).count());
    }

    @Test
    void categoryRefreshFailureIsNonFatal() {
        service.connect("K1", new RecordingEmitter());
        when(adminPortalClient.refreshCategories()).thenThrow(new RuntimeException("down"));
        assertDoesNotThrow(service::pollAdminPortal);
        verify(adminPortalClient).refreshProducts();
    }

    private static AdminCategoryDto cat(String code, String name, int priority) {
        return new AdminCategoryDto(code, name, null, priority);
    }

    /** Ket noi da hong: send va complete deu nem loi nhu Tomcat sau khi client ngat. */
    private static final class BrokenEmitter extends SseEmitter {
        @Override
        public void send(SseEventBuilder builder) throws IOException {
            throw new IOException("broken pipe");
        }

        @Override
        public synchronized void complete() {
            throw new IllegalStateException("Response not usable after response errors.");
        }
    }

    /** Ghi lai ten su kien da gui. */
    private static final class RecordingEmitter extends SseEmitter {
        final List<String> events = new ArrayList<>();

        @Override
        public void send(SseEventBuilder builder) {
            StringBuilder raw = new StringBuilder();
            builder.build().forEach(d -> raw.append(d.getData()));
            for (String line : raw.toString().split("\n")) {
                if (line.startsWith("event:")) {
                    events.add(line.substring("event:".length()));
                }
            }
        }
    }
}
