package vn.baotinmanhhai.kioskapi.ops;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.baotinmanhhai.kioskapi.catalog.entity.ChannelConfigEntity;
import vn.baotinmanhhai.kioskapi.catalog.repo.ChannelConfigRepository;
import vn.baotinmanhhai.kioskapi.catalog.service.ChannelConfigService;
import vn.baotinmanhhai.kioskapi.common.ApiException;
import vn.baotinmanhhai.kioskapi.ops.dto.OpsChannelConfigDto;
import vn.baotinmanhhai.kioskapi.price.service.PriceStreamService;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/** Doc/sua cau hinh kenh KIOSK (ban ghi singleton id=1). Sua xong -> SSE "config-changed". */
@Service
public class OpsChannelConfigService {

    private static final Pattern HH_MM = Pattern.compile("([01]\\d|2[0-3]):[0-5]\\d");
    private static final DateTimeFormatter HH_MM_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final ChannelConfigService channelConfigService;
    private final ChannelConfigRepository channelConfigRepository;
    private final PriceStreamService priceStreamService;

    public OpsChannelConfigService(ChannelConfigService channelConfigService,
                                   ChannelConfigRepository channelConfigRepository,
                                   PriceStreamService priceStreamService) {
        this.channelConfigService = channelConfigService;
        this.channelConfigRepository = channelConfigRepository;
        this.priceStreamService = priceStreamService;
    }

    /** Gio mo/dong da kiem: ca hai null (khong gioi han gio) hoac ca hai dung "HH:mm". */
    record Hours(LocalTime open, LocalTime close) {
    }

    public OpsChannelConfigDto get() {
        return toDto(channelConfigService.get());
    }

    public OpsChannelConfigDto update(OpsChannelConfigDto req) {
        Hours hours = parseHours(req.openTime(), req.closeTime());
        ChannelConfigEntity entity = channelConfigService.get();
        entity.setOpenTime(hours.open());
        entity.setCloseTime(hours.close());
        entity.setHotline(req.hotline().trim());
        entity.setIdleTimeoutSeconds(req.idleTimeoutSeconds());
        entity.setStalePriceSeconds(req.stalePriceSeconds());
        entity.setMinAppVersion(req.minAppVersion().trim());
        entity.setChannelPaused(req.channelPaused());
        // enableHkd khong mo cho web admin - giu nguyen gia tri dang co tren entity.
        ChannelConfigEntity saved = channelConfigRepository.save(entity);
        priceStreamService.notifyConfigChanged();
        return toDto(saved);
    }

    static Hours parseHours(String openTime, String closeTime) {
        String open = blankToNull(openTime);
        String close = blankToNull(closeTime);
        if (open == null && close == null) {
            return new Hours(null, null);
        }
        if (open == null || close == null) {
            throw badRequest("openTime va closeTime phai cung de trong hoac cung co gia tri");
        }
        if (!HH_MM.matcher(open).matches() || !HH_MM.matcher(close).matches()) {
            throw badRequest("openTime/closeTime phai co dinh dang HH:mm");
        }
        return new Hours(LocalTime.parse(open, HH_MM_FORMAT), LocalTime.parse(close, HH_MM_FORMAT));
    }

    static OpsChannelConfigDto toDto(ChannelConfigEntity e) {
        return new OpsChannelConfigDto(
                e.getOpenTime() == null ? null : e.getOpenTime().format(HH_MM_FORMAT),
                e.getCloseTime() == null ? null : e.getCloseTime().format(HH_MM_FORMAT),
                e.getHotline(), e.getIdleTimeoutSeconds(), e.getStalePriceSeconds(), e.getMinAppVersion(),
                e.isChannelPaused());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "400", message);
    }
}
