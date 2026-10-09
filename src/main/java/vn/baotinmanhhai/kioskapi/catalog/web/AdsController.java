package vn.baotinmanhhai.kioskapi.catalog.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.baotinmanhhai.kioskapi.catalog.dto.AdDto;
import vn.baotinmanhhai.kioskapi.catalog.service.AdService;
import vn.baotinmanhhai.kioskapi.common.ApiResponse;

import java.util.List;

/** II.2.2 GET /api/v1/kiosk/ads/playlist. Bo sung ETag o day cho don gian (khong tach filter rieng). */
@RestController
public class AdsController {

    private final AdService adService;

    public AdsController(AdService adService) {
        this.adService = adService;
    }

    @GetMapping("/api/v1/kiosk/ads/playlist")
    public ApiResponse<List<AdDto>> playlist() {
        return ApiResponse.success(adService.listPlaylist());
    }
}
