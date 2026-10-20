package vn.baotinmanhhai.kioskapi.catalog.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.baotinmanhhai.kioskapi.catalog.adminclient.AdminPortalClient;
import vn.baotinmanhhai.kioskapi.catalog.adminclient.AdminStoreDto;
import vn.baotinmanhhai.kioskapi.catalog.dto.ProvinceDto;
import vn.baotinmanhhai.kioskapi.catalog.dto.StoreDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * II.2.9/II.2.10: chi tinh co cua hang moi duoc tra; id tinh/cua hang la id SO (spec), lay tu
 * province_internal_id/internal_id cua admin-portal. Ma hanh chinh thuc: 01=Ha Noi, 79=Ho Chi Minh.
 */
class ProvinceStoreServiceTest {

    private AdminPortalClient adminPortalClient;
    private ProvinceStoreService service;

    @BeforeEach
    void setUp() {
        adminPortalClient = mock(AdminPortalClient.class);
        service = new ProvinceStoreService(adminPortalClient);
    }

    @Test
    void excludesProvincesWithoutAnyStore() {
        when(adminPortalClient.getStores()).thenReturn(List.of(store(11L, 1L, "01", "Hà Nội")));

        List<ProvinceDto> result = service.listWithStores();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("01", result.get(0).code());
    }

    @Test
    void hanoiAndHcmArePinnedFirstWhenBothHaveStores() {
        when(adminPortalClient.getStores()).thenReturn(List.of(
                store(1L, 3L, "48", "Đà Nẵng"),
                store(2L, 2L, "79", "Hồ Chí Minh"),
                store(3L, 1L, "01", "Hà Nội")
        ));

        List<ProvinceDto> result = service.listWithStores();

        assertEquals(3, result.size());
        assertEquals("01", result.get(0).code());
        assertEquals("79", result.get(1).code());
        assertEquals("48", result.get(2).code());
    }

    @Test
    void storesByProvinceUseNumericIds() {
        when(adminPortalClient.getStores()).thenReturn(List.of(
                store(7L, 1L, "01", "Hà Nội"),
                store(8L, 2L, "79", "Hồ Chí Minh")
        ));

        List<StoreDto> result = service.listByProvince(1L);

        assertEquals(1, result.size());
        assertEquals(7L, result.get(0).id());
        assertEquals(1L, result.get(0).provinceId());
    }

    private static AdminStoreDto store(long id, long provinceId, String provinceCode, String provinceName) {
        return new AdminStoreDto("S" + id, id, provinceId, null, "Store " + id, "Dia chi",
                provinceCode, provinceName, null, null, 1, null);
    }
}
