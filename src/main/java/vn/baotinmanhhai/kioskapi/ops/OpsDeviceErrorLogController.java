package vn.baotinmanhhai.kioskapi.ops;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.baotinmanhhai.kioskapi.catalog.dto.PageResponse;
import vn.baotinmanhhai.kioskapi.common.ApiResponse;
import vn.baotinmanhhai.kioskapi.common.TimeUtil;
import vn.baotinmanhhai.kioskapi.device.entity.DeviceErrorLogEntity;
import vn.baotinmanhhai.kioskapi.device.repo.DeviceErrorLogRepository;
import vn.baotinmanhhai.kioskapi.ops.dto.OpsDeviceErrorLogDto;

import java.time.format.DateTimeFormatter;

/** Web admin xem log loi may gui len (II.2.13), moi nhat truoc, loc tuy chon theo deviceCode. */
@RestController
@RequestMapping("/internal/ops/device-error-logs")
public class OpsDeviceErrorLogController {

    private static final int MAX_SIZE = 200;

    private final DeviceErrorLogRepository deviceErrorLogRepository;

    public OpsDeviceErrorLogController(DeviceErrorLogRepository deviceErrorLogRepository) {
        this.deviceErrorLogRepository = deviceErrorLogRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<OpsDeviceErrorLogDto>> list(
            @RequestParam(required = false) String deviceCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_SIZE),
                Sort.by(Sort.Direction.DESC, "receivedAt"));
        Page<DeviceErrorLogEntity> result = deviceCode == null || deviceCode.isBlank()
                ? deviceErrorLogRepository.findAll(pageable)
                : deviceErrorLogRepository.findByDeviceCode(deviceCode.trim(), pageable);
        return ApiResponse.success(PageResponse.of(result, OpsDeviceErrorLogController::toDto));
    }

    private static OpsDeviceErrorLogDto toDto(DeviceErrorLogEntity e) {
        return new OpsDeviceErrorLogDto(e.getId(), e.getDeviceCode(), e.getErrorId(), e.getType(), e.getMessage(),
                e.getOccurredAt(),
                e.getReceivedAt() == null ? null
                        : e.getReceivedAt().atZone(TimeUtil.VIETNAM_ZONE).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                e.getContext());
    }
}
