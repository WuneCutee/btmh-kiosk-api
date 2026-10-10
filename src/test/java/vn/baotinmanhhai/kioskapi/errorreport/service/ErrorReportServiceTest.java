package vn.baotinmanhhai.kioskapi.errorreport.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.baotinmanhhai.kioskapi.common.ApiException;
import vn.baotinmanhhai.kioskapi.device.entity.DeviceErrorLogEntity;
import vn.baotinmanhhai.kioskapi.device.repo.DeviceErrorLogRepository;
import vn.baotinmanhhai.kioskapi.errorreport.dto.ErrorItemDto;
import vn.baotinmanhhai.kioskapi.errorreport.dto.ErrorReportRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * II.2.13: context qua 2048 byte phai bi tu choi (400) MA KHONG duoc ghi DB mot phan cac
 * phan tu hop le dung truoc no trong cung mang errors[] - validate het roi moi ghi, khong xen ke.
 */
class ErrorReportServiceTest {

    private DeviceErrorLogRepository repository;
    private ErrorReportService service;

    @BeforeEach
    void setUp() {
        repository = mock(DeviceErrorLogRepository.class);
        service = new ErrorReportService(repository, new ObjectMapper());
    }

    @Test
    void rejectsOversizedContextWithoutPersistingEarlierValidItems() {
        ErrorReportRequest request = new ErrorReportRequest();
        ErrorItemDto ok = item("id-1", null);
        ErrorItemDto tooLong = item("id-2", Map.of("blob", "x".repeat(3000)));
        request.setErrors(List.of(ok, tooLong));

        assertThrows(ApiException.class, () -> service.report("KIOSK-TT-01", request));

        verify(repository, never()).save(any());
    }

    @Test
    void duplicateErrorIdIsSkippedButStillSucceeds() {
        when(repository.existsByDeviceCodeAndErrorId("KIOSK-TT-01", "id-1")).thenReturn(true);
        ErrorReportRequest request = new ErrorReportRequest();
        request.setErrors(List.of(item("id-1", null)));

        service.report("KIOSK-TT-01", request);

        verify(repository, never()).save(any());
    }

    @Test
    void newNonDuplicateItemIsPersisted() {
        when(repository.existsByDeviceCodeAndErrorId(any(), any())).thenReturn(false);
        ErrorReportRequest request = new ErrorReportRequest();
        request.setErrors(List.of(item("id-1", null)));

        service.report("KIOSK-TT-01", request);

        verify(repository, times(1)).save(any(DeviceErrorLogEntity.class));
    }

    private static ErrorItemDto item(String id, Map<String, Object> context) {
        ErrorItemDto item = new ErrorItemDto();
        item.setId(id);
        item.setOccurredAt("2026-09-17T10:00:00+07:00");
        item.setType("OTHER");
        item.setMessage("test message");
        item.setContext(context);
        return item;
    }
}
