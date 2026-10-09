package vn.baotinmanhhai.kioskapi.catalog.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.baotinmanhhai.kioskapi.catalog.dto.ProvinceDto;
import vn.baotinmanhhai.kioskapi.catalog.dto.StoreDto;
import vn.baotinmanhhai.kioskapi.catalog.service.ProvinceStoreService;
import vn.baotinmanhhai.kioskapi.common.ApiResponse;

import java.util.List;

/** II.2.9/II.2.10. */
@RestController
public class ProvinceStoreController {

    private final ProvinceStoreService service;

    public ProvinceStoreController(ProvinceStoreService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/miniapp/provinces/with-stores")
    public ApiResponse<List<ProvinceDto>> provincesWithStores() {
        return ApiResponse.success(service.listWithStores());
    }

    @GetMapping("/api/v1/miniapp/stores/by-province")
    public ApiResponse<List<StoreDto>> storesByProvince(@RequestParam long provinceId) {
        return ApiResponse.success(service.listByProvince(provinceId));
    }
}
