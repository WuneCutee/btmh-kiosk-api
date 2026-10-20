package vn.baotinmanhhai.kioskapi.catalog.service;

import org.springframework.stereotype.Service;
import vn.baotinmanhhai.kioskapi.catalog.adminclient.AdminPortalClient;
import vn.baotinmanhhai.kioskapi.catalog.dto.LimitDto;

/** II.2.11 - doc tu admin-portal (GET /api/limits), nguon catalog duy nhat (xem README). */
@Service
public class LimitService {

    private final AdminPortalClient adminPortalClient;

    public LimitService(AdminPortalClient adminPortalClient) {
        this.adminPortalClient = adminPortalClient;
    }

    public LimitDto getActive() {
        return new LimitDto(adminPortalClient.getActiveLimit().limitQuantity());
    }
}
