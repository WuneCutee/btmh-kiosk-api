package vn.baotinmanhhai.kioskapi.ops;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.baotinmanhhai.kioskapi.catalog.entity.AdEntity;
import vn.baotinmanhhai.kioskapi.catalog.repo.AdRepository;
import vn.baotinmanhhai.kioskapi.common.ApiException;
import vn.baotinmanhhai.kioskapi.ops.dto.OpsAdDto;
import vn.baotinmanhhai.kioskapi.ops.dto.OpsAdRequest;
import vn.baotinmanhhai.kioskapi.price.service.PriceStreamService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** Kiem actionType -> truong dich bat buoc, xoa truong dich thua; gioi han 20 quang cao; chi tai lai media khi doi url. */
class OpsAdServiceTest {

    private AdRepository adRepository;
    private MediaInspector mediaInspector;
    private PriceStreamService priceStreamService;
    private OpsAdService service;

    @BeforeEach
    void setUp() {
        adRepository = mock(AdRepository.class);
        mediaInspector = mock(MediaInspector.class);
        priceStreamService = mock(PriceStreamService.class);
        service = new OpsAdService(adRepository, mediaInspector, priceStreamService);
        when(adRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void noneClearsAllTargets() {
        OpsAdService.Normalized n = OpsAdService.normalize(req("IMAGE", "NONE", "p1", "c1", "https://x.vn"));
        assertEquals("NONE", n.actionType());
        assertNull(n.productId());
        assertNull(n.categoryId());
        assertNull(n.websiteUrl());
    }

    @Test
    void productRequiresProductIdAndClearsOthers() {
        assertThrows(ApiException.class, () -> OpsAdService.normalize(req("IMAGE", "PRODUCT", null, "c1", null)));
        OpsAdService.Normalized n = OpsAdService.normalize(req("IMAGE", "PRODUCT", "p1", "c1", "https://x.vn"));
        assertEquals("p1", n.productId());
        assertNull(n.categoryId());
        assertNull(n.websiteUrl());
    }

    @Test
    void categoryRequiresCategoryId() {
        assertThrows(ApiException.class, () -> OpsAdService.normalize(req("IMAGE", "CATEGORY", "p1", " ", null)));
        assertEquals("c1", OpsAdService.normalize(req("IMAGE", "CATEGORY", null, "c1", null)).categoryId());
    }

    @Test
    void websiteRequiresHttpUrl() {
        assertThrows(ApiException.class, () -> OpsAdService.normalize(req("IMAGE", "WEBSITE", null, null, null)));
        assertThrows(ApiException.class, () -> OpsAdService.normalize(req("IMAGE", "WEBSITE", null, null, "ftp://x.vn")));
        assertEquals("https://x.vn", OpsAdService.normalize(req("IMAGE", "WEBSITE", null, null, "https://x.vn")).websiteUrl());
    }

    @Test
    void rejectsUnknownMediaAndActionTypeAndNonHttpUrl() {
        ApiException ex = assertThrows(ApiException.class, () -> OpsAdService.normalize(req("GIF", "NONE", null, null, null)));
        assertEquals("400", ex.getCode());
        assertThrows(ApiException.class, () -> OpsAdService.normalize(req("IMAGE", "OPEN", null, null, null)));
        OpsAdRequest badUrl = new OpsAdRequest("Ad", "IMAGE", "file:///etc/passwd", 1, "NONE", null, null, null, 10, true);
        assertThrows(ApiException.class, () -> OpsAdService.normalize(badUrl));
    }

    @Test
    void createRejectedWhenTwentyAdsExistWithoutDownloading() {
        when(adRepository.count()).thenReturn(20L);
        assertThrows(ApiException.class, () -> service.create(req("IMAGE", "NONE", null, null, null)));
        verifyNoInteractions(mediaInspector);
        verify(adRepository, never()).save(any());
        verify(priceStreamService, never()).notifyPlaylistChanged();
    }

    @Test
    void createStoresChecksumAndNotifies() {
        when(adRepository.count()).thenReturn(3L);
        when(mediaInspector.inspect(anyString(), anyString())).thenReturn(new MediaInspector.MediaInfo("abc", 42));
        OpsAdDto dto = service.create(req("IMAGE", "NONE", null, null, null));
        assertEquals("abc", dto.checksum());
        assertEquals(42, dto.sizeBytes());
        assertTrue(dto.enabled());
        verify(priceStreamService).notifyPlaylistChanged();
    }

    @Test
    void updateKeepsChecksumWhenUrlAndMediaTypeUnchanged() {
        AdEntity existing = existing();
        when(adRepository.findById("a1")).thenReturn(Optional.of(existing));
        OpsAdDto dto = service.update("a1", req("IMAGE", "NONE", null, null, null));
        verifyNoInteractions(mediaInspector);
        assertEquals("old", dto.checksum());
        verify(priceStreamService).notifyPlaylistChanged();
    }

    @Test
    void updateRedownloadsWhenUrlChanged() {
        when(adRepository.findById("a1")).thenReturn(Optional.of(existing()));
        when(mediaInspector.inspect("https://cdn.vn/b.jpg", "IMAGE")).thenReturn(new MediaInspector.MediaInfo("new", 7));
        OpsAdRequest changed = new OpsAdRequest("Ad", "IMAGE", "https://cdn.vn/b.jpg", 1, "NONE",
                null, null, null, 10, null);
        OpsAdDto dto = service.update("a1", changed);
        assertEquals("new", dto.checksum());
        assertEquals(7, dto.sizeBytes());
    }

    @Test
    void updateMissingIs404() {
        when(adRepository.findById("x")).thenReturn(Optional.empty());
        ApiException ex = assertThrows(ApiException.class, () -> service.update("x", req("IMAGE", "NONE", null, null, null)));
        assertEquals("404", ex.getCode());
    }

    private static AdEntity existing() {
        AdEntity a = new AdEntity();
        a.setId("a1");
        a.setName("Ad");
        a.setMediaType("IMAGE");
        a.setUrl("https://cdn.vn/a.jpg");
        a.setOrder(1);
        a.setActionType("NONE");
        a.setChecksum("old");
        a.setSizeBytes(1);
        a.setDisplaySeconds(10);
        return a;
    }

    private static OpsAdRequest req(String mediaType, String actionType, String productId, String categoryId,
                                    String websiteUrl) {
        return new OpsAdRequest("Ad", mediaType, "https://cdn.vn/a.jpg", 1, actionType,
                productId, categoryId, websiteUrl, 10, null);
    }
}
