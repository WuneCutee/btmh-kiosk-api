package vn.baotinmanhhai.kioskapi.catalog.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import vn.baotinmanhhai.kioskapi.catalog.dto.AdDto;
import vn.baotinmanhhai.kioskapi.catalog.service.AdService;
import vn.baotinmanhhai.kioskapi.common.ApiResponse;
import vn.baotinmanhhai.kioskapi.common.EtagSupport;

import java.util.List;

/**
 * II.2.2 GET /api/v1/kiosk/ads/playlist. "Ho tro ETag/If-None-Match: khong doi -> 304 Not
 * Modified, khong co body" - ETag la hash noi dung danh sach (xem EtagSupport), WebRequest.
 * checkNotModified() tu so sanh voi header If-None-Match cua request va tu set 304 + header ETag
 * khi khop, khong can tu viet tay.
 */
@RestController
public class AdsController {

    private final AdService adService;
    private final EtagSupport etagSupport;

    public AdsController(AdService adService, EtagSupport etagSupport) {
        this.adService = adService;
        this.etagSupport = etagSupport;
    }

    @GetMapping("/api/v1/kiosk/ads/playlist")
    public ResponseEntity<ApiResponse<List<AdDto>>> playlist(WebRequest webRequest) {
        List<AdDto> playlist = adService.listPlaylist();
        String etag = etagSupport.hashOf(playlist);
        if (webRequest.checkNotModified(etag)) {
            return null; // Spring da ghi 304 + header ETag vao response, khong con body.
        }
        return ResponseEntity.ok(ApiResponse.success(playlist));
    }
}
