package vn.baotinmanhhai.kioskapi.ops;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.baotinmanhhai.kioskapi.catalog.adminclient.AdminPortalClient;
import vn.baotinmanhhai.kioskapi.common.ApiResponse;
import vn.baotinmanhhai.kioskapi.price.service.PriceStreamService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Web admin (kiosk-admin) bao "catalog vua doi" ngay sau khi luu, de kiosk-api kiem tra lai va day
 * SSE trong ~1 giay thay vi doi vong poll 5 giay (vong poll van chay, lam du phong khi lo bao).
 * Bao ve bang X-Ops-Key (OpsAuthFilter). Tra 202 ngay, phan doc admin-portal chay nen.
 */
@RestController
@RequestMapping("/internal/ops/catalog-changed")
public class OpsCatalogController {

    private final AdminPortalClient adminPortalClient;
    private final PriceStreamService priceStreamService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "catalog-changed");
        t.setDaemon(true);
        return t;
    });

    public OpsCatalogController(AdminPortalClient adminPortalClient, PriceStreamService priceStreamService) {
        this.adminPortalClient = adminPortalClient;
        this.priceStreamService = priceStreamService;
    }

    @PostMapping
    public org.springframework.http.ResponseEntity<ApiResponse<Void>> changed() {
        adminPortalClient.invalidateAll();
        executor.execute(priceStreamService::pollAdminPortal);
        return org.springframework.http.ResponseEntity.accepted().body(ApiResponse.success());
    }
}
