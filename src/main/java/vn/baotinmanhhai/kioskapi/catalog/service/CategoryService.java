package vn.baotinmanhhai.kioskapi.catalog.service;

import org.springframework.stereotype.Service;
import vn.baotinmanhhai.kioskapi.catalog.adminclient.AdminPortalClient;
import vn.baotinmanhhai.kioskapi.catalog.dto.CategoryDto;

import java.util.Comparator;
import java.util.List;

/** II.2.5 - doc tu admin-portal (GET /api/categories), nguon catalog duy nhat (xem README). */
@Service
public class CategoryService {

    private final AdminPortalClient adminPortalClient;

    public CategoryService(AdminPortalClient adminPortalClient) {
        this.adminPortalClient = adminPortalClient;
    }

    public List<CategoryDto> list() {
        return adminPortalClient.getCategories().stream()
                .sorted(Comparator.comparing(c -> c.priority() == null ? Integer.MAX_VALUE : c.priority()))
                .map(c -> new CategoryDto(c.categoryCode(), c.categoryCode(), c.categoryName(), c.description()))
                .toList();
    }
}
