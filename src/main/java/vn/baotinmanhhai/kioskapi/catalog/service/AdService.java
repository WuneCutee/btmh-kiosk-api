package vn.baotinmanhhai.kioskapi.catalog.service;

import org.springframework.stereotype.Service;
import vn.baotinmanhhai.kioskapi.catalog.dto.AdDto;
import vn.baotinmanhhai.kioskapi.catalog.repo.AdRepository;
import vn.baotinmanhhai.kioskapi.common.TimeUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;

/** II.2.2. */
@Service
public class AdService {

    private final AdRepository adRepository;

    public AdService(AdRepository adRepository) {
        this.adRepository = adRepository;
    }

    public List<AdDto> listPlaylist() {
        return adRepository.findByEnabledTrueOrderByOrderAsc().stream()
                .map(a -> new AdDto(
                        a.getId(), a.getName(), a.getMediaType(), a.getUrl(), a.getOrder(), a.getActionType(),
                        a.getProductId(), a.getCategoryId(), a.getWebsiteUrl(), a.getChecksum(), a.getSizeBytes(),
                        a.getUpdatedAt().atZone(TimeUtil.VIETNAM_ZONE).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        a.getDisplaySeconds()))
                .toList();
    }
}
