package vn.baotinmanhhai.kioskapi.ops;

import org.junit.jupiter.api.Test;
import vn.baotinmanhhai.kioskapi.catalog.entity.ChannelConfigEntity;
import vn.baotinmanhhai.kioskapi.catalog.repo.ChannelConfigRepository;
import vn.baotinmanhhai.kioskapi.catalog.service.ChannelConfigService;
import vn.baotinmanhhai.kioskapi.common.ApiException;
import vn.baotinmanhhai.kioskapi.ops.dto.OpsChannelConfigDto;
import vn.baotinmanhhai.kioskapi.price.service.PriceStreamService;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** openTime/closeTime: ca hai null hoac ca hai "HH:mm" hop le; sua xong giu enableHkd va bao config-changed. */
class OpsChannelConfigServiceTest {

    @Test
    void bothNullMeansNoHours() {
        OpsChannelConfigService.Hours hours = OpsChannelConfigService.parseHours(null, "  ");
        assertNull(hours.open());
        assertNull(hours.close());
    }

    @Test
    void validHoursAreParsed() {
        OpsChannelConfigService.Hours hours = OpsChannelConfigService.parseHours("08:30", "21:00");
        assertEquals(LocalTime.of(8, 30), hours.open());
        assertEquals(LocalTime.of(21, 0), hours.close());
    }

    @Test
    void onlyOneSideIsRejected() {
        ApiException ex = assertThrows(ApiException.class, () -> OpsChannelConfigService.parseHours("08:00", null));
        assertEquals("400", ex.getCode());
        assertThrows(ApiException.class, () -> OpsChannelConfigService.parseHours(null, "21:00"));
    }

    @Test
    void malformedTimesAreRejected() {
        assertThrows(ApiException.class, () -> OpsChannelConfigService.parseHours("8:00", "21:00"));
        assertThrows(ApiException.class, () -> OpsChannelConfigService.parseHours("08:00", "24:00"));
        assertThrows(ApiException.class, () -> OpsChannelConfigService.parseHours("08:60", "21:00"));
        assertThrows(ApiException.class, () -> OpsChannelConfigService.parseHours("08:00:00", "21:00"));
    }

    @Test
    void updateKeepsEnableHkdAndNotifies() {
        ChannelConfigService channelConfigService = mock(ChannelConfigService.class);
        ChannelConfigRepository repository = mock(ChannelConfigRepository.class);
        PriceStreamService priceStreamService = mock(PriceStreamService.class);
        ChannelConfigEntity entity = new ChannelConfigEntity();
        entity.setEnableHkd(true);
        when(channelConfigService.get()).thenReturn(entity);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        OpsChannelConfigService service = new OpsChannelConfigService(channelConfigService, repository, priceStreamService);

        OpsChannelConfigDto saved = service.update(
                new OpsChannelConfigDto("08:00", "22:00", "1800.1234", 90, 600, "1.2.3", true));

        assertTrue(entity.isEnableHkd());
        assertEquals("08:00", saved.openTime());
        assertEquals("22:00", saved.closeTime());
        assertEquals("1.2.3", saved.minAppVersion());
        assertTrue(saved.channelPaused());
        verify(priceStreamService).notifyConfigChanged();
    }
}
