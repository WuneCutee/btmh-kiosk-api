package vn.baotinmanhhai.kioskapi.ops;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.baotinmanhhai.kioskapi.common.ApiException;
import vn.baotinmanhhai.kioskapi.common.ApiResponse;
import vn.baotinmanhhai.kioskapi.common.TimeUtil;
import vn.baotinmanhhai.kioskapi.device.entity.DeviceEntity;
import vn.baotinmanhhai.kioskapi.device.repo.DeviceRepository;
import vn.baotinmanhhai.kioskapi.device.service.ActivationCodeService;
import vn.baotinmanhhai.kioskapi.ops.dto.ActivationCodeResponse;
import vn.baotinmanhhai.kioskapi.ops.dto.CreateDeviceRequest;
import vn.baotinmanhhai.kioskapi.price.service.PriceStreamService;

import java.time.format.DateTimeFormatter;

/**
 * KHONG thuoc tai lieu tich hop API Kiosk (xem ghi chu o OpsAuthFilter va README). Chi dung de
 * bring-up/test standalone du an nay vi khong co admin UI rieng trong pham vi cong viec.
 */
@RestController
@RequestMapping("/internal/ops/devices")
public class OpsDeviceController {

    private final ActivationCodeService activationCodeService;
    private final DeviceRepository deviceRepository;
    private final PriceStreamService priceStreamService;

    public OpsDeviceController(ActivationCodeService activationCodeService, DeviceRepository deviceRepository,
                                PriceStreamService priceStreamService) {
        this.activationCodeService = activationCodeService;
        this.deviceRepository = deviceRepository;
        this.priceStreamService = priceStreamService;
    }

    @PostMapping
    public org.springframework.http.ResponseEntity<ApiResponse<ActivationCodeResponse>> create(
            @Valid @RequestBody CreateDeviceRequest req) {
        ActivationCodeService.IssuedActivationCode issued = activationCodeService
                .createPendingDeviceAndIssueCode(req.getDeviceCode(), req.getStoreId(), req.getStoreName());
        ActivationCodeResponse body = new ActivationCodeResponse(
                issued.deviceCode(), issued.activationCode(),
                issued.expiresAt().atZone(TimeUtil.VIETNAM_ZONE).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        return org.springframework.http.ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(body));
    }

    @PostMapping("/{deviceCode}/revoke")
    @Transactional
    public ApiResponse<Void> revoke(@PathVariable String deviceCode) {
        DeviceEntity device = deviceRepository.findById(deviceCode)
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.NOT_FOUND, "404", "Khong tim thay may: " + deviceCode));
        device.setStatus(DeviceEntity.STATUS_REVOKED);
        deviceRepository.save(device);
        priceStreamService.revokeDevice(deviceCode);
        return ApiResponse.success();
    }
}
