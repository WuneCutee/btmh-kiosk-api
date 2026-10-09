package vn.baotinmanhhai.kioskapi.catalog.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.baotinmanhhai.kioskapi.catalog.dto.CategoryDto;
import vn.baotinmanhhai.kioskapi.catalog.service.CategoryService;
import vn.baotinmanhhai.kioskapi.common.ApiResponse;

import java.util.List;

/** II.2.5 GET /api/v1/miniapp/categories. */
@RestController
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/api/v1/miniapp/categories")
    public ApiResponse<List<CategoryDto>> categories() {
        return ApiResponse.success(categoryService.list());
    }
}
