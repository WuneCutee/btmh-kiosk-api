package vn.baotinmanhhai.kioskapi.catalog.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import vn.baotinmanhhai.kioskapi.catalog.dto.PageResponse;
import vn.baotinmanhhai.kioskapi.catalog.dto.ProductDetailDto;
import vn.baotinmanhhai.kioskapi.catalog.dto.ProductImageDto;
import vn.baotinmanhhai.kioskapi.catalog.dto.ProductListItemDto;
import vn.baotinmanhhai.kioskapi.catalog.entity.ProductEntity;
import vn.baotinmanhhai.kioskapi.catalog.repo.ProductImageRepository;
import vn.baotinmanhhai.kioskapi.catalog.repo.ProductRepository;
import vn.baotinmanhhai.kioskapi.common.ErrorCodes;
import vn.baotinmanhhai.kioskapi.common.TimeUtil;
import vn.baotinmanhhai.kioskapi.common.VietnameseText;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** II.2.6/II.2.7/II.2.8. */
@Service
public class ProductService {

    public static final String CATEGORY_ALL = "ALL";
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    public ProductService(ProductRepository productRepository, ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
    }

    public PageResponse<ProductListItemDto> list(String categoryId, int pageNumber, Integer pageSize) {
        int size = resolvePageSize(pageSize);
        PageRequest pageable = PageRequest.of(pageNumber, size);
        Page<ProductEntity> page = CATEGORY_ALL.equalsIgnoreCase(categoryId)
                ? productRepository.findAllAcrossCategories(pageable)
                : productRepository.findByCategory(categoryId, pageable);
        return toPageResponse(page);
    }

    /**
     * II.2.7: "khong phan biet hoa/thuong va khong phan biet dau" - LOWER()/LIKE thuan cua SQL
     * khong bo dau duoc, nen loc TRONG JAVA bang VietnameseText.fold() thay vi SQL. Danh sach goc
     * da duoc DB sap dung thu tu (giong II.2.6) nen chi can loc giu nguyen thu tu roi tu phan
     * trang bang PageImpl - khong can sort lai.
     */
    public PageResponse<ProductListItemDto> search(String rawName, String categoryId, Integer pageNumber, Integer pageSize) {
        String foldedQuery = VietnameseText.fold(rawName == null ? "" : rawName.trim());
        int size = resolvePageSize(pageSize);
        int number = pageNumber == null ? 0 : pageNumber;
        PageRequest pageable = PageRequest.of(number, size);
        boolean all = categoryId == null || categoryId.isBlank() || CATEGORY_ALL.equalsIgnoreCase(categoryId);

        List<ProductEntity> candidates = all
                ? productRepository.findAllEnabledPricedAcrossCategories()
                : productRepository.findAllEnabledPricedByCategory(categoryId);
        List<ProductEntity> matched = candidates.stream()
                .filter(p -> VietnameseText.fold(p.getName()).contains(foldedQuery))
                .toList();

        int from = Math.min((int) pageable.getOffset(), matched.size());
        int to = Math.min(from + pageable.getPageSize(), matched.size());
        Page<ProductEntity> page = new PageImpl<>(matched.subList(from, to), pageable, matched.size());
        return toPageResponse(page);
    }

    public ProductDetailDto detail(String id) {
        ProductEntity product = productRepository.findByIdAndEnabledTrueAndPriceIsNotNull(id)
                .orElseThrow(ErrorCodes::productGone);
        List<ProductImageDto> images = toImageDtos(product.getId());
        return new ProductDetailDto(
                product.getId(), product.getName(), product.getCategoryId(), product.getCode(),
                product.getQuantity(), product.getPrice(), product.getBuyPrice(),
                formatIso(product), product.getRemainingQuantity(), product.getDescription(),
                images, false, product.getPriceId(), product.getServiceCode()
        );
    }

    private PageResponse<ProductListItemDto> toPageResponse(Page<ProductEntity> page) {
        List<String> ids = page.getContent().stream().map(ProductEntity::getId).toList();
        Map<String, List<ProductImageDto>> imagesByProduct = productImageRepository
                .findByProductIdInOrderByPriorityAsc(ids).stream()
                .collect(Collectors.groupingBy(
                        img -> img.getProductId(),
                        Collectors.mapping(img -> new ProductImageDto(img.getUrl(), img.getPriority()), Collectors.toList())));
        return PageResponse.of(page, p -> new ProductListItemDto(
                p.getId(), p.getName(), p.getCategoryId(), p.getCode(), p.getQuantity(), p.getPrice(),
                p.getBuyPrice(), formatIso(p), p.getRemainingQuantity(),
                imagesByProduct.getOrDefault(p.getId(), List.of()), false, p.getPriceId(), p.getServiceCode()));
    }

    private List<ProductImageDto> toImageDtos(String productId) {
        return productImageRepository.findByProductIdOrderByPriorityAsc(productId).stream()
                .map(img -> new ProductImageDto(img.getUrl(), img.getPriority()))
                .toList();
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

    private static String formatIso(ProductEntity p) {
        return p.getPriceUpdatedAt().atZone(TimeUtil.VIETNAM_ZONE).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
