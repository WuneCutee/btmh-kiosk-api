package vn.baotinmanhhai.kioskapi.price.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import vn.baotinmanhhai.kioskapi.price.service.PriceStreamService;
import vn.baotinmanhhai.kioskapi.ratelimit.RateLimiterService;

/** II.3 GET /api/v1/kiosk/prices/stream. */
@RestController
public class PriceStreamController {

    private final PriceStreamService priceStreamService;
    private final RateLimiterService rateLimiterService;

    public PriceStreamController(PriceStreamService priceStreamService, RateLimiterService rateLimiterService) {
        this.priceStreamService = priceStreamService;
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping(value = "/api/v1/kiosk/prices/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> stream(HttpServletRequest request) {
        String deviceCode = (String) request.getAttribute("deviceCode");
        rateLimiterService.checkAndIncrement("sse-connect:" + deviceCode, 6);
        SseEmitter emitter = priceStreamService.connect(deviceCode);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .header("X-Accel-Buffering", "no")
                .body(emitter);
    }
}
