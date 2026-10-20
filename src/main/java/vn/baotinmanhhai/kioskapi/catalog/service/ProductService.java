package vn.baotinmanhhai.kioskapi.catalog.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import vn.baotinmanhhai.kioskapi.catalog.adminclient.AdminPortalClient;
import vn.baotinmanhhai.kioskapi.catalog.adminclient.AdminProductDto;
import vn.baotinmanhhai.kioskapi.catalog.dto.PageResponse;
import vn.baotinmanhhai.kioskapi.catalog.dto.ProductDetailDto;
import vn.baotinmanhhai.kioskapi.catalog.dto.ProductImageDto;
import vn.baotinmanhhai.kioskapi.catalog.dto.ProductListItemDto;
import vn.baotinmanhhai.kioskapi.common.ErrorCodes;
import vn.baotinmanhhai.kioskapi.common.VietnameseText;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * II.2.6/II.2.7/II.2.8 - doc tu admin-portal (GET /api/products), nguon catalog duy nhat (xem
 * README muc "Nguon catalog"). admin-portal da loc san pham active+visible+co gia (cung quy tac
 * "San pham chua co gia khong xuat hien" cua tai lieu) nen chi loc THEM status=Active+sell!=null
 * phong khi data bi thieu, roi phan trang/tim kiem trong Java (danh muc nho, phu hop cache).
 */
@Service
public class ProductService {

    public static final String CATEGORY_ALL = "ALL";
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private final AdminPortalClient adminPortalClient;

    public ProductService(AdminPortalClient adminPortalClient) {
        this.adminPortalClient = adminPortalClient;
    }

    public PageResponse<ProductListItemDto> list(String categoryId, int pageNumber, Integer pageSize) {
        int size = resolvePageSize(pageSize);
        List<AdminProductDto> candidates = enabledPriced(categoryId);
        return paginate(candidates, pageNumber, size);
    }

    /**
     * II.2.7: "khong phan biet hoa/thuong va khong phan biet dau" - loc trong Java bang
     * VietnameseText.fold() thay vi SQL (giong thiet ke cu).
     */
    public PageResponse<ProductListItemDto> search(String rawName, String categoryId, Integer pageNumber, Integer pageSize) {
        String foldedQuery = VietnameseText.fold(rawName == null ? "" : rawName.trim());
        int size = resolvePageSize(pageSize);
        int number = pageNumber == null ? 0 : pageNumber;

        List<AdminProductDto> matched = enabledPriced(categoryId).stream()
                .filter(p -> VietnameseText.fold(p.productName()).contains(foldedQuery))
                .toList();
        return paginate(matched, number, size);
    }

    public ProductDetailDto detail(String id) {
        AdminProductDto p = adminPortalClient.getProducts().stream()
                .filter(x -> id.equals(x.productId()) && isEnabledPriced(x))
                .findFirst()
                .orElseThrow(ErrorCodes::productGone);
        return new ProductDetailDto(
                p.productId(), p.productName(), p.categoryCode(), p.productCode(),
                p.quantity(), parsePrice(p.sell()), parseOptionalPrice(p.buy()),
                p.updatedAt(), remainingQuantity(p), p.description(),
                toImageDtos(p), false, null, null
        );
    }

    /** Dung thu tu cua II.2.6 categoryId=ALL - SSE (II.3.1) lay "order" theo chinh danh sach nay. */
    public List<AdminProductDto> allEnabledPricedInListOrder() {
        return enabledPriced(CATEGORY_ALL);
    }

    private List<AdminProductDto> enabledPriced(String categoryId) {
        boolean all = categoryId == null || categoryId.isBlank() || CATEGORY_ALL.equalsIgnoreCase(categoryId);
        return adminPortalClient.getProducts().stream()
                .filter(this::isEnabledPriced)
                .filter(p -> all || categoryId.equals(p.categoryCode()))
                .sorted(Comparator.comparing(AdminProductDto::quantity,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private boolean isEnabledPriced(AdminProductDto p) {
        return "Active".equalsIgnoreCase(p.status()) && p.sell() != null;
    }

    private PageResponse<ProductListItemDto> paginate(List<AdminProductDto> source, int pageNumber, int pageSize) {
        PageRequest pageable = PageRequest.of(pageNumber, pageSize);
        int from = Math.min((int) pageable.getOffset(), source.size());
        int to = Math.min(from + pageable.getPageSize(), source.size());
        Page<AdminProductDto> page = new PageImpl<>(source.subList(from, to), pageable, source.size());
        return PageResponse.of(page, p -> new ProductListItemDto(
                p.productId(), p.productName(), p.categoryCode(), p.productCode(), p.quantity(),
                parsePrice(p.sell()), parseOptionalPrice(p.buy()), p.updatedAt(), remainingQuantity(p),
                toImageDtos(p), false, null, null));
    }

    private List<ProductImageDto> toImageDtos(AdminProductDto p) {
        if (p.images() == null) {
            return List.of();
        }
        return p.images().stream()
                .map(i -> new ProductImageDto(i.url(), i.priority() == null ? 0 : i.priority()))
                .toList();
    }

    private int remainingQuantity(AdminProductDto p) {
        return p.reserve() == null ? 0 : p.reserve();
    }

    private long parsePrice(String value) {
        return value == null ? 0L : new BigDecimal(value).longValueExact();
    }

    private Long parseOptionalPrice(String value) {
        return value == null ? null : new BigDecimal(value).longValueExact();
    }

    private int resolvePageSize(Integer requested) {
        if (requested == null) {
            return DEFAULT_PAGE_SIZE;
        }
        if (requested > MAX_PAGE_SIZE) {
            throw ErrorCodes.validationError(List.of(
                    new vn.baotinmanhhai.kioskapi.common.ValidationErrorItem("pageSize",
                            vn.baotinmanhhai.kioskapi.common.ValidationErrorItem.TOO_LONG)));
        }
        return requested;
    }
}
