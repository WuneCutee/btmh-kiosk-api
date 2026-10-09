package vn.baotinmanhhai.kioskapi.catalog.service;

import org.springframework.stereotype.Service;
import vn.baotinmanhhai.kioskapi.catalog.dto.LimitDto;
import vn.baotinmanhhai.kioskapi.catalog.entity.TransactionLimitEntity;
import vn.baotinmanhhai.kioskapi.catalog.repo.TransactionLimitRepository;

/** II.2.11. */
@Service
public class LimitService {

    private final TransactionLimitRepository repository;

    public LimitService(TransactionLimitRepository repository) {
        this.repository = repository;
    }

    public LimitDto getActive() {
        return repository.findById(TransactionLimitEntity.SINGLETON_ID)
                .map(e -> new LimitDto(e.getLimitQuantity()))
                .orElse(new LimitDto(null));
    }
}
