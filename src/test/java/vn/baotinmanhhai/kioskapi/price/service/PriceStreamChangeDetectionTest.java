package vn.baotinmanhhai.kioskapi.price.service;

import org.junit.jupiter.api.Test;
import vn.baotinmanhhai.kioskapi.catalog.adminclient.AdminProductDto;
import vn.baotinmanhhai.kioskapi.price.dto.PriceItemDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * II.3.1: sua tren web admin phai ve App dung loai su kien - doi gia/ton -> price (chi san pham doi, kem
 * xu huong), doi ten/anh/bat-tat -> snapshot (App tai lai danh sach), khong doi gi -> khong gui.
 */
class PriceStreamChangeDetectionTest {

    @Test
    void firstReadOnlyTakesBaseline() {
        List<AdminProductDto> v1 = List.of(product("p1", "Nhẫn", "1000000", "img1"));
        PriceStreamService.Change change = classify(List.of(), v1);
        assertEquals("none", change.event());
    }

    @Test
    void priceChangeSendsOnlyChangedProductWithTrend() {
        List<AdminProductDto> v1 = List.of(product("p1", "Nhẫn", "1000000", "img1"), product("p2", "Lắc", "2000000", "img2"));
        List<AdminProductDto> v2 = List.of(product("p1", "Nhẫn", "1200000", "img1"), product("p2", "Lắc", "2000000", "img2"));
        PriceStreamService.Change change = classify(v1, v2);
        assertEquals("price", change.event());
        assertEquals(1, change.items().size());
        assertEquals("p1", change.items().get(0).productId());
        assertEquals("UP", change.items().get(0).priceTrend());
    }

    @Test
    void nameChangeSendsSnapshot() {
        List<AdminProductDto> v1 = List.of(product("p1", "Nhẫn", "1000000", "img1"));
        List<AdminProductDto> v2 = List.of(product("p1", "Nhẫn vàng 24K", "1000000", "img1"));
        assertEquals("snapshot", classify(v1, v2).event());
    }

    @Test
    void imageChangeSendsSnapshot() {
        List<AdminProductDto> v1 = List.of(product("p1", "Nhẫn", "1000000", "img1"));
        List<AdminProductDto> v2 = List.of(product("p1", "Nhẫn", "1000000", "img-new"));
        assertEquals("snapshot", classify(v1, v2).event());
    }

    @Test
    void disablingProductSendsSnapshot() {
        List<AdminProductDto> v1 = List.of(product("p1", "Nhẫn", "1000000", "img1"), product("p2", "Lắc", "2000000", "img2"));
        List<AdminProductDto> v2 = List.of(product("p1", "Nhẫn", "1000000", "img1"));
        assertEquals("snapshot", classify(v1, v2).event());
    }

    @Test
    void noChangeSendsNothing() {
        List<AdminProductDto> v1 = List.of(product("p1", "Nhẫn", "1000000", "img1"));
        assertEquals("none", classify(v1, v1).event());
    }

    private static PriceStreamService.Change classify(List<AdminProductDto> before, List<AdminProductDto> after) {
        Map<String, PriceItemDto> prev = PriceStreamService.toItems(before, Map.of());
        Map<String, PriceItemDto> cur = PriceStreamService.toItems(after, prev);
        return PriceStreamService.classify(prev, PriceStreamService.contentFingerprints(before),
                cur, PriceStreamService.contentFingerprints(after));
    }

    private static AdminProductDto product(String id, String name, String sell, String img) {
        return new AdminProductDto(id, id.toUpperCase(), name, sell, null, "Active", "CAT",
                new BigDecimal("1.00"), "mo ta", List.of(new AdminProductDto.Image(img, 0)),
                "2026-10-16T09:00:00+07:00", 5);
    }
}
