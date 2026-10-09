package vn.baotinmanhhai.kioskapi.catalog.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import vn.baotinmanhhai.kioskapi.catalog.dto.BootstrapDto;
import vn.baotinmanhhai.kioskapi.catalog.dto.SystemConfigDto;
import vn.baotinmanhhai.kioskapi.catalog.service.BootstrapService;
import vn.baotinmanhhai.kioskapi.common.ApiResponse;
import vn.baotinmanhhai.kioskapi.common.EtagSupport;

/**
 * II.2.3 (legacy, gioi han mac dinh 60/phut) + II.2.12 (bootstrap, gioi han rieng 10/phut, co ho
 * tro ETag/If-None-Match giong II.2.2). Gioi han da kiem tap trung o DeviceAuthFilter
 * (RATE_LIMIT_OVERRIDES theo tung duong dan).
 */
@RestController
public class BootstrapController {

    private final BootstrapService bootstrapService;
    private final EtagSupport etagSupport;

    public BootstrapController(BootstrapService bootstrapService, EtagSupport etagSupport) {
        this.bootstrapService = bootstrapService;
        this.etagSupport = etagSupport;
    }

    @GetMapping("/api/v1/miniapp/system-configs")
    public ApiResponse<SystemConfigDto> systemConfigs() {
        return ApiResponse.success(bootstrapService.legacySystemConfig());
    }

    @GetMapping("/api/v1/kiosk/bootstrap")
    public ResponseEntity<ApiResponse<BootstrapDto>> bootstrap(HttpServletRequest request, WebRequest webRequest) {
        String deviceCode = (String) request.getAttribute("deviceCode");
        BootstrapDto dto = bootstrapService.bootstrap(deviceCode);
        // serverTime doi moi lan goi (gio hien tai) - KHONG duoc dua vao hash ETag, neu khong ETag
        // se khong bao gio trung nhau giua 2 lan goi va 304 se khong bao gio xay ra. Chi hash phan
        // "on dinh" (chi doi khi cau hinh/thong tin may thuc su doi).
        Object stablePart = new Object[]{
                dto.openTime(), dto.closeTime(), dto.hotline(), dto.idleTimeoutSeconds(),
                dto.stalePriceSeconds(), dto.minAppVersion(), dto.storeId(), dto.storeName()
        };
        String etag = etagSupport.hashOf(stablePart);
        if (webRequest.checkNotModified(etag)) {
            return null;
        }
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
