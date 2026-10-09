package vn.baotinmanhhai.kioskapi.catalog.web;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import vn.baotinmanhhai.kioskapi.catalog.dto.PageResponse;
import vn.baotinmanhhai.kioskapi.catalog.dto.ProductDetailDto;
import vn.baotinmanhhai.kioskapi.catalog.dto.ProductListItemDto;
import vn.baotinmanhhai.kioskapi.catalog.dto.ProductSearchRequest;
import vn.baotinmanhhai.kioskapi.catalog.service.ProductService;
import vn.baotinmanhhai.kioskapi.common.ApiResponse;
import vn.baotinmanhhai.kioskapi.common.ErrorCodes;

/** II.2.6/II.2.7/II.2.8. */
@RestController
@RequestMapping("/api/v1/miniapp/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ProductListItemDto>> list(
            @RequestParam String categoryId,
            @RequestParam int pageNumber,
            @RequestParam(required = false) Integer pageSize) {
        if (categoryId == null || categoryId.isBlank()) {
            throw ErrorCodes.missingParameter("categoryId");
        }
        return ApiResponse.success(productService.list(categoryId, pageNumber, pageSize));
    }

    @PostMapping("/search")
    public ApiResponse<PageResponse<ProductListItemDto>> search(@Valid @RequestBody ProductSearchRequest req) {
        return ApiResponse.success(productService.search(req.getName(), req.getCategoryId(), req.getPageNumber(), req.getPageSize()));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDetailDto> detail(@PathVariable String id) {
        return ApiResponse.success(productService.detail(id));
    }
}
