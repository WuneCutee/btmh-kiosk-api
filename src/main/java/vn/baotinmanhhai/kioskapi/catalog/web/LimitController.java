package vn.baotinmanhhai.kioskapi.catalog.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.baotinmanhhai.kioskapi.catalog.dto.LimitDto;
import vn.baotinmanhhai.kioskapi.catalog.service.LimitService;
import vn.baotinmanhhai.kioskapi.common.ApiResponse;

/** II.2.11 GET /api/v1/miniapp/limits/active. */
@RestController
public class LimitController {

    private final LimitService limitService;

    public LimitController(LimitService limitService) {
        this.limitService = limitService;
    }

    @GetMapping("/api/v1/miniapp/limits/active")
    public ApiResponse<LimitDto> active() {
        return ApiResponse.success(limitService.getActive());
    }
}
