package vn.baotinmanhhai.kioskapi.ops;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.baotinmanhhai.kioskapi.common.ApiResponse;
import vn.baotinmanhhai.kioskapi.common.TimeUtil;
import vn.baotinmanhhai.kioskapi.device.service.ActivationCodeService;
import vn.baotinmanhhai.kioskapi.ops.dto.ActivationCodeResponse;
import vn.baotinmanhhai.kioskapi.ops.dto.CreateDeviceRequest;
import vn.baotinmanhhai.kioskapi.ops.dto.OpsDeviceDto;
import vn.baotinmanhhai.kioskapi.ops.dto.OpsDeviceUpdateRequest;
import vn.baotinmanhhai.kioskapi.price.service.PriceStreamService;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * KHONG thuoc tai lieu tich hop API Kiosk (xem ghi chu o OpsAuthFilter va README). Web admin
 * (kiosk-admin) quan ly may: tao/xem/sua/cap lai ma/ep xoay khoa/thu hoi/xoa.
 *
 * <p>Thao tac DB nam trong OpsDeviceService (@Transactional); dong SSE / bao config-changed goi SAU khi
 * service tra ve (da commit), de loi SSE khong lam rollback va khong bien thanh 500.
 */
@RestController
@RequestMapping("/internal/ops/devices")
public class OpsDeviceController {

    private final ActivationCodeService activationCodeService;
    private final OpsDeviceService opsDeviceService;
    private final PriceStreamService priceStreamService;

    public OpsDeviceController(ActivationCodeService activationCodeService, OpsDeviceService opsDeviceService,
                                PriceStreamService priceStreamService) {
        this.activationCodeService = activationCodeService;
        this.opsDeviceService = opsDeviceService;
        this.priceStreamService = priceStreamService;
    }

    @GetMapping
    public ApiResponse<List<OpsDeviceDto>> list() {
        return ApiResponse.success(opsDeviceService.list());
    }

    @GetMapping("/{deviceCode}")
    public ApiResponse<OpsDeviceDto> get(@PathVariable String deviceCode) {
        return ApiResponse.success(opsDeviceService.get(deviceCode));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ActivationCodeResponse>> create(@Valid @RequestBody CreateDeviceRequest req) {
        ActivationCodeService.IssuedActivationCode issued = activationCodeService
                .createPendingDeviceAndIssueCode(req.getDeviceCode(), req.getStoreId(), req.getStoreName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toResponse(issued)));
    }

    @PutMapping("/{deviceCode}")
    public ApiResponse<OpsDeviceDto> update(@PathVariable String deviceCode,
                                            @Valid @RequestBody OpsDeviceUpdateRequest req) {
        OpsDeviceDto updated = opsDeviceService.update(deviceCode, req);
        priceStreamService.notifyConfigChanged();
        return ApiResponse.success(updated);
    }

    @PostMapping("/{deviceCode}/activation-code")
    public ApiResponse<ActivationCodeResponse> reissueActivationCode(@PathVariable String deviceCode) {
        return ApiResponse.success(toResponse(activationCodeService.reissueActivationCode(deviceCode)));
    }

    @PostMapping("/{deviceCode}/force-key-rotation")
    public ApiResponse<OpsDeviceDto> forceKeyRotation(@PathVariable String deviceCode) {
        return ApiResponse.success(opsDeviceService.forceKeyRotation(deviceCode));
    }

    @PostMapping("/{deviceCode}/revoke")
    public ApiResponse<Void> revoke(@PathVariable String deviceCode) {
        opsDeviceService.revoke(deviceCode);
        priceStreamService.revokeDevice(deviceCode);
        return ApiResponse.success();
    }

    @DeleteMapping("/{deviceCode}")
    public ApiResponse<Void> delete(@PathVariable String deviceCode) {
        opsDeviceService.delete(deviceCode);
        priceStreamService.revokeDevice(deviceCode);
        return ApiResponse.success();
    }

    private static ActivationCodeResponse toResponse(ActivationCodeService.IssuedActivationCode issued) {
        return new ActivationCodeResponse(issued.deviceCode(), issued.activationCode(),
                issued.expiresAt().atZone(TimeUtil.VIETNAM_ZONE).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }
}
