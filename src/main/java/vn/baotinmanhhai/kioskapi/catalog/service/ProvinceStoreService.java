package vn.baotinmanhhai.kioskapi.catalog.service;

import org.springframework.stereotype.Service;
import vn.baotinmanhhai.kioskapi.catalog.adminclient.AdminPortalClient;
import vn.baotinmanhhai.kioskapi.catalog.adminclient.AdminStoreDto;
import vn.baotinmanhhai.kioskapi.catalog.dto.ProvinceDto;
import vn.baotinmanhhai.kioskapi.catalog.dto.StoreDto;
import vn.baotinmanhhai.kioskapi.common.TimeUtil;

import java.text.Collator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/** II.2.9/II.2.10 - doc tu admin-portal (GET /api/stores), nguon catalog duy nhat (xem README). */
@Service
public class ProvinceStoreService {

    private static final String HANOI_CODE = "01";
    private static final String HCM_CODE = "79";

    private final AdminPortalClient adminPortalClient;
    private final Collator vietnameseCollator;

    public ProvinceStoreService(AdminPortalClient adminPortalClient) {
        this.adminPortalClient = adminPortalClient;
        // java khong co Locale "vi" built-in day du, nhung Collator cua default locale "vi" (neu
        // JVM ho tro) xap xi dung bang chu cai tieng Viet; chap nhan duoc cho pham vi du an nay.
        this.vietnameseCollator = Collator.getInstance(Locale.forLanguageTag("vi"));
    }

    /**
     * II.2.9: "Lay cac tinh/thanh CO cua hang nhan hang BAT cho kenh Kiosk" - tinh/thanh khong co
     * cua hang bat nao PHAI bi loai khoi danh sach (khong phai tra storeCount=0).
     */
    public List<ProvinceDto> listWithStores() {
        Map<Long, List<AdminStoreDto>> byProvince = adminPortalClient.getStores().stream()
                .filter(s -> s.provinceInternalId() != null && s.internalId() != null)
                .collect(Collectors.groupingBy(AdminStoreDto::provinceInternalId, LinkedHashMap::new, Collectors.toList()));

        return byProvince.entrySet().stream()
                .map(e -> {
                    AdminStoreDto any = e.getValue().get(0);
                    return new ProvinceDto(e.getKey(), any.provinceId(), any.provinceName(), e.getValue().size());
                })
                .sorted(provinceComparator())
                .toList();
    }

    public List<StoreDto> listByProvince(long provinceId) {
        LocalDate today = LocalDate.now(TimeUtil.VIETNAM_ZONE);
        return adminPortalClient.getStores().stream()
                .filter(s -> s.internalId() != null && Long.valueOf(provinceId).equals(s.provinceInternalId()))
                .sorted(storeComparator())
                .map(s -> {
                    int days = s.numberDaysReceive() == null ? 0 : s.numberDaysReceive();
                    return new StoreDto(
                            s.internalId(), s.name(), s.address(), s.provinceInternalId(), s.wardInternalId(),
                            s.numberDaysReceive(), s.wardName(),
                            today.plusDays(days).toString(), null);
                })
                .toList();
    }

    private Comparator<ProvinceDto> provinceComparator() {
        return Comparator
                .comparing((ProvinceDto p) -> priorityOf(p.code()))
                .thenComparing(ProvinceDto::name, vietnameseCollator::compare);
    }

    private int priorityOf(String code) {
        if (HANOI_CODE.equals(code)) {
            return 0;
        }
        if (HCM_CODE.equals(code)) {
            return 1;
        }
        return 2;
    }

    private Comparator<AdminStoreDto> storeComparator() {
        return Comparator
                .comparing((AdminStoreDto s) -> s.wardName() == null ? "" : s.wardName(), vietnameseCollator::compare)
                .thenComparing(AdminStoreDto::name, vietnameseCollator::compare);
    }
}
