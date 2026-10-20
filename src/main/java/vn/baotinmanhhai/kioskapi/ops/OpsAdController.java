package vn.baotinmanhhai.kioskapi.ops;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.baotinmanhhai.kioskapi.common.ApiResponse;
import vn.baotinmanhhai.kioskapi.ops.dto.OpsAdDto;
import vn.baotinmanhhai.kioskapi.ops.dto.OpsAdRequest;

import java.util.List;

/** Web admin (kiosk-admin) quan tri quang cao man cho. Bao ve bang X-Ops-Key (OpsAuthFilter). */
@RestController
@RequestMapping("/internal/ops/ads")
public class OpsAdController {

    private final OpsAdService opsAdService;

    public OpsAdController(OpsAdService opsAdService) {
        this.opsAdService = opsAdService;
    }

    @GetMapping
    public ApiResponse<List<OpsAdDto>> list() {
        return ApiResponse.success(opsAdService.list());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OpsAdDto>> create(@Valid @RequestBody OpsAdRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(opsAdService.create(req)));
    }

    @PutMapping("/{id}")
    public ApiResponse<OpsAdDto> update(@PathVariable String id, @Valid @RequestBody OpsAdRequest req) {
        return ApiResponse.success(opsAdService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        opsAdService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/toggle")
    public ApiResponse<OpsAdDto> toggle(@PathVariable String id) {
        return ApiResponse.success(opsAdService.toggle(id));
    }
}
