package vn.baotinmanhhai.kioskapi.catalog.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.baotinmanhhai.kioskapi.catalog.dto.BannerDto;
import vn.baotinmanhhai.kioskapi.catalog.service.BannerService;
import vn.baotinmanhhai.kioskapi.common.ApiResponse;

import java.util.List;

/** II.2.4 GET /api/v1/miniapp/banners. */
@RestController
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping("/api/v1/miniapp/banners")
    public ApiResponse<List<BannerDto>> banners() {
        return ApiResponse.success(bannerService.list());
    }
}
