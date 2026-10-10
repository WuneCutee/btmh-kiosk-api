package vn.baotinmanhhai.kioskapi.catalog.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.baotinmanhhai.kioskapi.catalog.dto.ProvinceDto;
import vn.baotinmanhhai.kioskapi.catalog.entity.ProvinceEntity;
import vn.baotinmanhhai.kioskapi.catalog.repo.ProvinceRepository;
import vn.baotinmanhhai.kioskapi.catalog.repo.StoreRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * II.2.9: "Lay cac tinh/thanh CO cua hang nhan hang BAT" - tinh enabled nhung storeCount=0 phai
 * bi LOAI khoi ket qua (khong phai tra ve voi storeCount=0). Phat hien qua test thu thuc te voi
 * Postgres: 2 tinh enabled, chi 1 tinh co cua hang, tinh kia (Ho Chi Minh) van bi tra ve sai.
 */
class ProvinceStoreServiceTest {

    private ProvinceRepository provinceRepository;
    private StoreRepository storeRepository;
    private ProvinceStoreService service;

    @BeforeEach
    void setUp() {
        provinceRepository = mock(ProvinceRepository.class);
        storeRepository = mock(StoreRepository.class);
        service = new ProvinceStoreService(provinceRepository, storeRepository);
    }

    @Test
    void excludesProvincesWithoutAnyEnabledStore() {
        ProvinceEntity hanoi = province(1L, "HN", "Hà Nội");
        ProvinceEntity hcm = province(2L, "HCM", "Hồ Chí Minh");
        when(provinceRepository.findByEnabledTrue()).thenReturn(List.of(hanoi, hcm));
        when(storeRepository.countByProvinceIdAndEnabledTrue(1L)).thenReturn(1L);
        when(storeRepository.countByProvinceIdAndEnabledTrue(2L)).thenReturn(0L);

        List<ProvinceDto> result = service.listWithStores();

        assertEquals(1, result.size());
        assertEquals("HN", result.get(0).code());
    }

    @Test
    void hanoiAndHcmArePinnedFirstWhenBothHaveStores() {
        ProvinceEntity hanoi = province(1L, "HN", "Hà Nội");
        ProvinceEntity hcm = province(2L, "HCM", "Hồ Chí Minh");
        ProvinceEntity danang = province(3L, "DN", "Đà Nẵng");
        when(provinceRepository.findByEnabledTrue()).thenReturn(List.of(danang, hcm, hanoi));
        when(storeRepository.countByProvinceIdAndEnabledTrue(1L)).thenReturn(2L);
        when(storeRepository.countByProvinceIdAndEnabledTrue(2L)).thenReturn(3L);
        when(storeRepository.countByProvinceIdAndEnabledTrue(3L)).thenReturn(1L);

        List<ProvinceDto> result = service.listWithStores();

        assertEquals(3, result.size());
        assertEquals("HN", result.get(0).code());
        assertEquals("HCM", result.get(1).code());
        assertEquals("DN", result.get(2).code());
    }

    private static ProvinceEntity province(long id, String code, String name) {
        ProvinceEntity p = new ProvinceEntity();
        p.setId(id);
        p.setCode(code);
        p.setName(name);
        p.setEnabled(true);
        return p;
    }
}
