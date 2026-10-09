package vn.baotinmanhhai.kioskapi.errorreport.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.baotinmanhhai.kioskapi.common.ApiResponse;
import vn.baotinmanhhai.kioskapi.errorreport.dto.ErrorReportRequest;
import vn.baotinmanhhai.kioskapi.errorreport.service.ErrorReportService;
import vn.baotinmanhhai.kioskapi.ratelimit.RateLimiterService;

/** II.2.13 POST /api/v1/kiosk/errors. */
@RestController
public class ErrorReportController {

    private final ErrorReportService errorReportService;
    private final RateLimiterService rateLimiterService;

    public ErrorReportController(ErrorReportService errorReportService, RateLimiterService rateLimiterService) {
        this.errorReportService = errorReportService;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/api/v1/kiosk/errors")
    public ApiResponse<Void> report(@Valid @RequestBody ErrorReportRequest request, HttpServletRequest httpRequest) {
        String deviceCode = (String) httpRequest.getAttribute("deviceCode");
        rateLimiterService.checkAndIncrement("errors:" + deviceCode, 1);
        errorReportService.report(deviceCode, request);
        return ApiResponse.success();
    }
}
