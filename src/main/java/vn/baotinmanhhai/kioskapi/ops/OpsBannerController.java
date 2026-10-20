package vn.baotinmanhhai.kioskapi.ops;

import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.baotinmanhhai.kioskapi.catalog.entity.BannerEntity;
import vn.baotinmanhhai.kioskapi.catalog.repo.BannerRepository;
import vn.baotinmanhhai.kioskapi.common.ApiException;
import vn.baotinmanhhai.kioskapi.common.ApiResponse;
import vn.baotinmanhhai.kioskapi.ops.dto.OpsBannerDto;
import vn.baotinmanhhai.kioskapi.ops.dto.OpsBannerRequest;
import vn.baotinmanhhai.kioskapi.price.service.PriceStreamService;

import java.util.List;
import java.util.UUID;

/**
 * Web admin quan tri banner (II.2.4). Moi lan ghi -> SSE "config-changed" de App tai lai banner.
 * Moi thao tac la 1 lenh save/delete (tu commit) roi moi bao SSE, tranh App doc truoc khi commit.
 */
@RestController
@RequestMapping("/internal/ops/banners")
public class OpsBannerController {

    private final BannerRepository bannerRepository;
    private final PriceStreamService priceStreamService;

    public OpsBannerController(BannerRepository bannerRepository, PriceStreamService priceStreamService) {
        this.bannerRepository = bannerRepository;
        this.priceStreamService = priceStreamService;
    }

    @GetMapping
    public ApiResponse<List<OpsBannerDto>> list() {
        return ApiResponse.success(bannerRepository.findAll(Sort.by("priority")).stream()
                .map(OpsBannerDto::from).toList());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OpsBannerDto>> create(@Valid @RequestBody OpsBannerRequest req) {
        BannerEntity banner = new BannerEntity();
        banner.setId(UUID.randomUUID().toString());
        apply(banner, req);
        BannerEntity saved = bannerRepository.save(banner);
        priceStreamService.notifyConfigChanged();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(OpsBannerDto.from(saved)));
    }

    @PutMapping("/{id}")
    public ApiResponse<OpsBannerDto> update(@PathVariable String id, @Valid @RequestBody OpsBannerRequest req) {
        BannerEntity banner = find(id);
        apply(banner, req);
        BannerEntity saved = bannerRepository.save(banner);
        priceStreamService.notifyConfigChanged();
        return ApiResponse.success(OpsBannerDto.from(saved));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        bannerRepository.delete(find(id));
        priceStreamService.notifyConfigChanged();
        return ApiResponse.success();
    }

    @PostMapping("/{id}/toggle")
    public ApiResponse<OpsBannerDto> toggle(@PathVariable String id) {
        BannerEntity banner = find(id);
        banner.setEnabled(!banner.isEnabled());
        BannerEntity saved = bannerRepository.save(banner);
        priceStreamService.notifyConfigChanged();
        return ApiResponse.success(OpsBannerDto.from(saved));
    }

    private BannerEntity find(String id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "404", "Khong tim thay banner: " + id));
    }

    private static void apply(BannerEntity banner, OpsBannerRequest req) {
        if (!OpsAdService.isHttpUrl(req.imagePath())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "400", "imagePath phai bat dau bang http:// hoac https://");
        }
        banner.setCode(req.code() == null || req.code().isBlank() ? null : req.code().trim());
        banner.setName(req.name().trim());
        banner.setImagePath(req.imagePath().trim());
        banner.setDescriptions(req.descriptions());
        banner.setPriority(req.priority());
        banner.setEnabled(req.enabled() == null || req.enabled());
    }
}
