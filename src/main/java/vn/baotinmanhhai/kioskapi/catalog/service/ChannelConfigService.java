package vn.baotinmanhhai.kioskapi.catalog.service;

import org.springframework.stereotype.Service;
import vn.baotinmanhhai.kioskapi.catalog.entity.ChannelConfigEntity;
import vn.baotinmanhhai.kioskapi.catalog.repo.ChannelConfigRepository;

@Service
public class ChannelConfigService {

    private final ChannelConfigRepository repository;

    public ChannelConfigService(ChannelConfigRepository repository) {
        this.repository = repository;
    }

    public ChannelConfigEntity get() {
        return repository.findById(ChannelConfigEntity.SINGLETON_ID)
                .orElseGet(() -> repository.save(new ChannelConfigEntity()));
    }

    public boolean isChannelPaused() {
        return get().isChannelPaused();
    }
}
