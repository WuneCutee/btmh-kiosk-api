package vn.baotinmanhhai.kioskapi.price.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import vn.baotinmanhhai.kioskapi.price.entity.PriceVersionEntity;

public interface PriceVersionRepository extends JpaRepository<PriceVersionEntity, Long> {

    // Transaction dat o day: PriceStreamService goi qua self-invocation nen @Transactional cua no khong co tac dung.
    @Transactional
    @Modifying
    @Query("UPDATE PriceVersionEntity v SET v.value = v.value + 1 WHERE v.id = 1")
    int incrementRow();
}
