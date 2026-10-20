package vn.baotinmanhhai.kioskapi.ops;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.baotinmanhhai.kioskapi.catalog.entity.AdEntity;
import vn.baotinmanhhai.kioskapi.catalog.repo.AdRepository;
import vn.baotinmanhhai.kioskapi.common.ApiException;
import vn.baotinmanhhai.kioskapi.ops.dto.OpsAdDto;
import vn.baotinmanhhai.kioskapi.ops.dto.OpsAdRequest;
import vn.baotinmanhhai.kioskapi.price.service.PriceStreamService;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Quan tri quang cao man cho (II.2.2) cho web admin. Tai media de tinh checksum/kich thuoc NGOAI
 * transaction (co the toi 30s) roi moi ghi; moi lan ghi thanh cong -> SSE "playlist-changed".
 */
@Service
public class OpsAdService {

    static final int MAX_ADS = 20;
    private static final Set<String> MEDIA_TYPES = Set.of(MediaInspector.MEDIA_IMAGE, MediaInspector.MEDIA_VIDEO);
    private static final String ACTION_NONE = "NONE";
    private static final String ACTION_PRODUCT = "PRODUCT";
    private static final String ACTION_CATEGORY = "CATEGORY";
    private static final String ACTION_WEBSITE = "WEBSITE";

    private final AdRepository adRepository;
    private final MediaInspector mediaInspector;
    private final PriceStreamService priceStreamService;

    public OpsAdService(AdRepository adRepository, MediaInspector mediaInspector,
                        PriceStreamService priceStreamService) {
        this.adRepository = adRepository;
        this.mediaInspector = mediaInspector;
        this.priceStreamService = priceStreamService;
    }

    /** Ket qua da chuan hoa tu request: enum viet hoa + chi giu dung truong dich cua actionType. */
    record Normalized(String mediaType, String actionType, String productId, String categoryId, String websiteUrl) {
    }

    public List<OpsAdDto> list() {
        return adRepository.findAll(Sort.by("order")).stream().map(OpsAdDto::from).toList();
    }

    public OpsAdDto create(OpsAdRequest req) {
        Normalized n = normalize(req);
        if (adRepository.count() >= MAX_ADS) {
            throw badRequest("Toi da " + MAX_ADS + " quang cao, xoa bot truoc khi them moi");
        }
        MediaInspector.MediaInfo media = mediaInspector.inspect(req.url().trim(), n.mediaType());
        AdEntity ad = new AdEntity();
        ad.setId(UUID.randomUUID().toString());
        apply(ad, req, n);
        ad.setChecksum(media.checksum());
        ad.setSizeBytes(media.sizeBytes());
        ad.setEnabled(req.enabled() == null || req.enabled());
        ad.setUpdatedAt(Instant.now());
        AdEntity saved = adRepository.save(ad);
        priceStreamService.notifyPlaylistChanged();
        return OpsAdDto.from(saved);
    }

    public OpsAdDto update(String id, OpsAdRequest req) {
        AdEntity ad = find(id);
        Normalized n = normalize(req);
        String url = req.url().trim();
        if (!Objects.equals(url, ad.getUrl()) || !Objects.equals(n.mediaType(), ad.getMediaType())) {
            MediaInspector.MediaInfo media = mediaInspector.inspect(url, n.mediaType());
            ad.setChecksum(media.checksum());
            ad.setSizeBytes(media.sizeBytes());
        }
        apply(ad, req, n);
        ad.setEnabled(req.enabled() == null || req.enabled());
        ad.setUpdatedAt(Instant.now());
        AdEntity saved = adRepository.save(ad);
        priceStreamService.notifyPlaylistChanged();
        return OpsAdDto.from(saved);
    }

    public void delete(String id) {
        adRepository.delete(find(id));
        priceStreamService.notifyPlaylistChanged();
    }

    public OpsAdDto toggle(String id) {
        AdEntity ad = find(id);
        ad.setEnabled(!ad.isEnabled());
        ad.setUpdatedAt(Instant.now());
        AdEntity saved = adRepository.save(ad);
        priceStreamService.notifyPlaylistChanged();
        return OpsAdDto.from(saved);
    }

    private AdEntity find(String id) {
        return adRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "404", "Khong tim thay quang cao: " + id));
    }

    private static void apply(AdEntity ad, OpsAdRequest req, Normalized n) {
        ad.setName(req.name().trim());
        ad.setMediaType(n.mediaType());
        ad.setUrl(req.url().trim());
        ad.setOrder(req.order());
        ad.setActionType(n.actionType());
        ad.setProductId(n.productId());
        ad.setCategoryId(n.categoryId());
        ad.setWebsiteUrl(n.websiteUrl());
        ad.setDisplaySeconds(req.displaySeconds());
    }

    /** Kiem mediaType/url/actionType va truong dich tuong ung; xoa cac truong dich khong dung. */
    static Normalized normalize(OpsAdRequest req) {
        String mediaType = upper(req.mediaType());
        if (!MEDIA_TYPES.contains(mediaType)) {
            throw badRequest("mediaType phai la IMAGE hoac VIDEO");
        }
        if (!isHttpUrl(req.url())) {
            throw badRequest("url phai bat dau bang http:// hoac https://");
        }
        String actionType = upper(req.actionType());
        String productId = blankToNull(req.productId());
        String categoryId = blankToNull(req.categoryId());
        String websiteUrl = blankToNull(req.websiteUrl());
        return switch (actionType) {
            case ACTION_NONE -> new Normalized(mediaType, actionType, null, null, null);
            case ACTION_PRODUCT -> {
                if (productId == null) {
                    throw badRequest("actionType PRODUCT can productId");
                }
                yield new Normalized(mediaType, actionType, productId, null, null);
            }
            case ACTION_CATEGORY -> {
                if (categoryId == null) {
                    throw badRequest("actionType CATEGORY can categoryId");
                }
                yield new Normalized(mediaType, actionType, null, categoryId, null);
            }
            case ACTION_WEBSITE -> {
                if (websiteUrl == null || !isHttpUrl(websiteUrl)) {
                    throw badRequest("actionType WEBSITE can websiteUrl bat dau bang http:// hoac https://");
                }
                yield new Normalized(mediaType, actionType, null, null, websiteUrl);
            }
            default -> throw badRequest("actionType phai la NONE, PRODUCT, CATEGORY hoac WEBSITE");
        };
    }

    static boolean isHttpUrl(String value) {
        if (value == null) {
            return false;
        }
        String v = value.trim().toLowerCase(Locale.ROOT);
        return (v.startsWith("http://") && v.length() > 7) || (v.startsWith("https://") && v.length() > 8);
    }

    private static String upper(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "400", message);
    }
}
