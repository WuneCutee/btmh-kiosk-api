package vn.baotinmanhhai.kioskapi.errorreport.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.baotinmanhhai.kioskapi.common.ApiResponse;
import vn.baotinmanhhai.kioskapi.errorreport.dto.ErrorReportRequest;
import vn.baotinmanhhai.kioskapi.errorreport.service.ErrorReportService;

/**
 * II.2.13 POST /api/v1/kiosk/errors. Gioi han 1/phut da kiem tap trung o DeviceAuthFilter
 * (RATE_LIMIT_OVERRIDES) truoc khi request toi day, khong tu goi RateLimiterService o day nua.
 */
@RestController
public class ErrorReportController {

    private final ErrorReportService errorReportService;

    public ErrorReportController(ErrorReportService errorReportService) {
        this.errorReportService = errorReportService;
    }

    @PostMapping("/api/v1/kiosk/errors")
    public ApiResponse<Void> report(@Valid @RequestBody ErrorReportRequest request, HttpServletRequest httpRequest) {
        String deviceCode = (String) httpRequest.getAttribute("deviceCode");
        errorReportService.report(deviceCode, request);
        return ApiResponse.success();
    }
}
