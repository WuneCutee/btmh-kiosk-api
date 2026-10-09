package vn.baotinmanhhai.kioskapi.device.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.baotinmanhhai.kioskapi.common.ApiResponse;
import vn.baotinmanhhai.kioskapi.common.ErrorCodes;
import vn.baotinmanhhai.kioskapi.common.ValidationErrorItem;
import vn.baotinmanhhai.kioskapi.device.dto.*;
import vn.baotinmanhhai.kioskapi.device.service.DeviceAuthService;
import vn.baotinmanhhai.kioskapi.ratelimit.RateLimiterService;

import java.util.List;

/**
 * Ba API xac thuc thiet bi KHONG dung Bearer: chu ky trong body (II.5.2). Khong co
 * DeviceAuthFilter nao chay truoc cac endpoint nay (xem WebSecurityConfig/DeviceAuthFilter).
 */
@RestController
public class DeviceAuthController {

    private final DeviceAuthService deviceAuthService;
    private final RateLimiterService rateLimiterService;

    public DeviceAuthController(DeviceAuthService deviceAuthService, RateLimiterService rateLimiterService) {
        this.deviceAuthService = deviceAuthService;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/api/v1/kiosk/device-sessions")
    public ResponseEntity<ApiResponse<DeviceSessionResponse>> deviceSessions(
            @Valid @RequestBody DeviceSessionRequest req,
            @RequestHeader(value = "X-Device-Code", required = false) String headerDeviceCode) {
        checkDeviceCodeHeader(req.getDeviceCode(), headerDeviceCode);
        rateLimiterService.checkAndIncrement("device-sessions:" + req.getDeviceCode(), 10);
        DeviceSessionResponse data = deviceAuthService.issueToken(req);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping("/api/v1/kiosk/devices/enroll")
    public ResponseEntity<ApiResponse<EnrollResponse>> enroll(
            @Valid @RequestBody EnrollRequest req,
            @RequestHeader(value = "X-Device-Code", required = false) String headerDeviceCode) {
        checkDeviceCodeHeader(req.getDeviceCode(), headerDeviceCode);
        rateLimiterService.checkAndIncrement("enroll:" + req.getDeviceCode(), 60);
        DeviceAuthService.EnrollResult result = deviceAuthService.enroll(req);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(ApiResponse.success(result.response()));
    }

    @PostMapping("/api/v1/kiosk/devices/rotate-key")
    public ResponseEntity<ApiResponse<RotateKeyResponse>> rotateKey(
            @Valid @RequestBody RotateKeyRequest req,
            @RequestHeader(value = "X-Device-Code", required = false) String headerDeviceCode) {
        checkDeviceCodeHeader(req.getDeviceCode(), headerDeviceCode);
        rateLimiterService.checkAndIncrement("rotate-key:" + req.getDeviceCode(), 60);
        RotateKeyResponse data = deviceAuthService.rotateKey(req);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    private void checkDeviceCodeHeader(String bodyDeviceCode, String headerDeviceCode) {
        if (headerDeviceCode != null && !headerDeviceCode.isBlank() && !headerDeviceCode.equals(bodyDeviceCode)) {
            throw ErrorCodes.validationError(List.of(new ValidationErrorItem("deviceCode", ValidationErrorItem.INVALID_FORMAT)));
        }
    }
}
