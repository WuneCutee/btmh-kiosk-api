package vn.baotinmanhhai.kioskapi.catalog.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.baotinmanhhai.kioskapi.catalog.dto.BootstrapDto;
import vn.baotinmanhhai.kioskapi.catalog.dto.SystemConfigDto;
import vn.baotinmanhhai.kioskapi.catalog.service.BootstrapService;
import vn.baotinmanhhai.kioskapi.common.ApiResponse;
import vn.baotinmanhhai.kioskapi.ratelimit.RateLimiterService;

/** II.2.3 (legacy) + II.2.12 (bootstrap). */
@RestController
public class BootstrapController {

    private final BootstrapService bootstrapService;
    private final RateLimiterService rateLimiterService;

    public BootstrapController(BootstrapService bootstrapService, RateLimiterService rateLimiterService) {
        this.bootstrapService = bootstrapService;
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/api/v1/miniapp/system-configs")
    public ApiResponse<SystemConfigDto> systemConfigs() {
        return ApiResponse.success(bootstrapService.legacySystemConfig());
    }

    @GetMapping("/api/v1/kiosk/bootstrap")
    public ApiResponse<BootstrapDto> bootstrap(HttpServletRequest request) {
        String deviceCode = (String) request.getAttribute("deviceCode");
        rateLimiterService.checkAndIncrement("bootstrap:" + deviceCode, 10);
        return ApiResponse.success(bootstrapService.bootstrap(deviceCode));
    }
}
