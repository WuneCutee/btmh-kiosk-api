package vn.baotinmanhhai.kioskapi.ops;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import vn.baotinmanhhai.kioskapi.common.ApiResponse;
import vn.baotinmanhhai.kioskapi.ops.dto.OpsChannelConfigDto;

/** Web admin xem/sua cau hinh kenh KIOSK (gio mo ban, hotline, idle, nguong gia cu, minAppVersion, tam dung). */
@RestController
@RequestMapping("/internal/ops/channel-config")
public class OpsChannelConfigController {

    private final OpsChannelConfigService opsChannelConfigService;

    public OpsChannelConfigController(OpsChannelConfigService opsChannelConfigService) {
        this.opsChannelConfigService = opsChannelConfigService;
    }

    @GetMapping
    public ApiResponse<OpsChannelConfigDto> get() {
        return ApiResponse.success(opsChannelConfigService.get());
    }

    @PutMapping
    public ApiResponse<OpsChannelConfigDto> update(@Valid @RequestBody OpsChannelConfigDto req) {
        return ApiResponse.success(opsChannelConfigService.update(req));
    }
}
