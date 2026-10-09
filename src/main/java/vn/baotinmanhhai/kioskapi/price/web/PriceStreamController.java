package vn.baotinmanhhai.kioskapi.price.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import vn.baotinmanhhai.kioskapi.price.service.PriceStreamService;

/**
 * II.3 GET /api/v1/kiosk/prices/stream. Gioi han 6 lan mo ket noi/phut da kiem tap trung o
 * DeviceAuthFilter (RATE_LIMIT_OVERRIDES) truoc khi request toi day.
 */
@RestController
public class PriceStreamController {

    private final PriceStreamService priceStreamService;

    public PriceStreamController(PriceStreamService priceStreamService) {
        this.priceStreamService = priceStreamService;
    }

    @GetMapping(value = "/api/v1/kiosk/prices/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> stream(HttpServletRequest request) {
        String deviceCode = (String) request.getAttribute("deviceCode");
        SseEmitter emitter = priceStreamService.connect(deviceCode);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .header("X-Accel-Buffering", "no")
                .body(emitter);
    }
}
