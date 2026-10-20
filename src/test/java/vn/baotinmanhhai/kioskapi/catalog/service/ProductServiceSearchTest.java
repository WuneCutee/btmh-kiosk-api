package vn.baotinmanhhai.kioskapi.catalog.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.baotinmanhhai.kioskapi.catalog.adminclient.AdminPortalClient;
import vn.baotinmanhhai.kioskapi.catalog.adminclient.AdminProductDto;
import vn.baotinmanhhai.kioskapi.catalog.dto.PageResponse;
import vn.baotinmanhhai.kioskapi.catalog.dto.ProductListItemDto;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * II.2.7: "Tim chuoi con, khong phan biet hoa/thuong va khong phan biet dau ('kim gia bao' tim
 * duoc 'Kim Gia Bảo')" - chung minh ProductService.search() thuc su loc dung tren du lieu doc tu
 * admin-portal (AdminPortalClient) + phan trang.
 */
class ProductServiceSearchTest {

    private AdminPortalClient adminPortalClient;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        adminPortalClient = mock(AdminPortalClient.class);
        productService = new ProductService(adminPortalClient);
    }

    @Test
    void findsAccentedNameByUnaccentedQuery() {
        when(adminPortalClient.getProducts()).thenReturn(List.of(
                product("p1", "Đồng Xu Kim Gia Bảo Hoa Sen"),
                product("p2", "Vòng tay bạc")
        ));

        PageResponse<ProductListItemDto> result = productService.search("kim gia bao", "ALL", 0, 10);

        assertEquals(1, result.totalElements());
        assertEquals("p1", result.content().get(0).id());
    }

    @Test
    void caseInsensitive() {
        when(adminPortalClient.getProducts()).thenReturn(List.of(
                product("p1", "Nhẫn Vàng 24K")
        ));

        PageResponse<ProductListItemDto> result = productService.search("NHẪN vàng", "ALL", 0, 10);

        assertEquals(1, result.totalElements());
    }

    @Test
    void noMatchReturnsEmptyContentNotError() {
        when(adminPortalClient.getProducts()).thenReturn(List.of(
                product("p1", "Nhẫn Vàng 24K")
        ));

        PageResponse<ProductListItemDto> result = productService.search("khong ton tai", "ALL", 0, 10);

        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
    }

    @Test
    void paginatesFilteredResultsCorrectly() {
        when(adminPortalClient.getProducts()).thenReturn(List.of(
                product("p1", "Vàng miếng loại 1"),
                product("p2", "Vàng miếng loại 2"),
                product("p3", "Vàng miếng loại 3"),
                product("p4", "Bạc miếng")
        ));

        PageResponse<ProductListItemDto> page0 = productService.search("vang mieng", "ALL", 0, 2);
        assertEquals(3, page0.totalElements());
        assertEquals(2, page0.totalPages());
        assertEquals(2, page0.content().size());
        assertTrue(page0.first());
        assertTrue(!page0.last());

        PageResponse<ProductListItemDto> page1 = productService.search("vang mieng", "ALL", 1, 2);
        assertEquals(1, page1.content().size());
        assertTrue(page1.last());
    }

    private static AdminProductDto product(String id, String name) {
        return new AdminProductDto(id, id.toUpperCase(), name, "1000000", null, "Active",
                "cat-1", new BigDecimal("1.00"), null, List.of(), "2026-10-15T00:00:00+07:00", 0);
    }
}
