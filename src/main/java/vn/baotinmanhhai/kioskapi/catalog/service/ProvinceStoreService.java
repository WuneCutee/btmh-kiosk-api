package vn.baotinmanhhai.kioskapi.catalog.service;

import org.springframework.stereotype.Service;
import vn.baotinmanhhai.kioskapi.catalog.dto.ProvinceDto;
import vn.baotinmanhhai.kioskapi.catalog.dto.StoreDto;
import vn.baotinmanhhai.kioskapi.catalog.entity.StoreEntity;
import vn.baotinmanhhai.kioskapi.catalog.repo.ProvinceRepository;
import vn.baotinmanhhai.kioskapi.catalog.repo.StoreRepository;
import vn.baotinmanhhai.kioskapi.common.TimeUtil;

import java.text.Collator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** II.2.9/II.2.10. */
@Service
public class ProvinceStoreService {

    private final ProvinceRepository provinceRepository;
    private final StoreRepository storeRepository;
    private final Collator vietnameseCollator;

    public ProvinceStoreService(ProvinceRepository provinceRepository, StoreRepository storeRepository) {
        this.provinceRepository = provinceRepository;
        this.storeRepository = storeRepository;
        // java khong co Locale "vi" built-in day du, nhung Collator cua default locale "vi" (neu
        // JVM ho tro) xap xi dung bang chu cai tieng Viet; chap nhan duoc cho pham vi du an nay.
        this.vietnameseCollator = Collator.getInstance(Locale.forLanguageTag("vi"));
    }

    /**
     * II.2.9: "Lay cac tinh/thanh CO cua hang nhan hang BAT cho kenh Kiosk" - tinh/thanh khong co
     * cua hang bat nao PHAI bi loai khoi danh sach (khong phai tra storeCount=0). Truoc day chi
     * loc theo kiosk_province.enabled ma khong loc storeCount>0, nen 1 tinh "enabled" nhung chua
     * co cua hang nao van bi tra ve voi storeCount=0 - sai voi mo ta chuc nang cua tai lieu.
     */
    public List<ProvinceDto> listWithStores() {
        return provinceRepository.findByEnabledTrue().stream()
                .map(p -> new ProvinceDto(p.getId(), p.getCode(), p.getName(),
                        storeRepository.countByProvinceIdAndEnabledTrue(p.getId())))
                .filter(dto -> dto.storeCount() > 0)
                .sorted(provinceComparator())
                .toList();
    }

    public List<StoreDto> listByProvince(long provinceId) {
        LocalDate today = LocalDate.now(TimeUtil.VIETNAM_ZONE);
        return storeRepository.findByProvinceIdAndEnabledTrue(provinceId).stream()
                .sorted(storeComparator())
                .map(s -> {
                    int days = s.getNumberDaysReceive() == null ? 0 : s.getNumberDaysReceive();
                    return new StoreDto(
                            s.getId(), s.getName(), s.getAddress(), s.getProvinceId(), s.getWardId(),
                            s.getNumberDaysReceive(), s.getWardName(),
                            today.plusDays(days).toString(), s.getPickupHours());
                })
                .toList();
    }

    private Comparator<ProvinceDto> provinceComparator() {
        return Comparator
                .comparing((ProvinceDto p) -> priorityOf(p.code()))
                .thenComparing(ProvinceDto::name, vietnameseCollator::compare);
    }

    private int priorityOf(String code) {
        if ("HN".equalsIgnoreCase(code)) {
            return 0;
        }
        if ("HCM".equalsIgnoreCase(code)) {
            return 1;
        }
        return 2;
    }

    private Comparator<StoreEntity> storeComparator() {
        return Comparator
                .comparing((StoreEntity s) -> s.getWardName() == null ? "" : s.getWardName(), vietnameseCollator::compare)
                .thenComparing(StoreEntity::getName, vietnameseCollator::compare);
    }
}
