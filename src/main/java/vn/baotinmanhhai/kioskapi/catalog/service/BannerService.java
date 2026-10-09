package vn.baotinmanhhai.kioskapi.catalog.service;

import org.springframework.stereotype.Service;
import vn.baotinmanhhai.kioskapi.catalog.dto.BannerDto;
import vn.baotinmanhhai.kioskapi.catalog.repo.BannerRepository;

import java.util.List;

/** II.2.4. */
@Service
public class BannerService {

    private final BannerRepository bannerRepository;

    public BannerService(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    public List<BannerDto> list() {
        return bannerRepository.findByEnabledTrueOrderByPriorityAsc().stream()
                .map(b -> new BannerDto(b.getId(), b.getCode(), b.getName(), b.getImagePath(), b.getDescriptions(), b.getPriority()))
                .toList();
    }
}
