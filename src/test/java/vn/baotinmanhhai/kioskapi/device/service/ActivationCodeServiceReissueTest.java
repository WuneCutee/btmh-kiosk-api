package vn.baotinmanhhai.kioskapi.device.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import vn.baotinmanhhai.kioskapi.common.ApiException;
import vn.baotinmanhhai.kioskapi.device.entity.ActivationCodeEntity;
import vn.baotinmanhhai.kioskapi.device.entity.DeviceEntity;
import vn.baotinmanhhai.kioskapi.device.repo.ActivationCodeRepository;
import vn.baotinmanhhai.kioskapi.device.repo.DeviceRepository;
import vn.baotinmanhhai.kioskapi.device.util.HashUtil;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/** Cap lai ma kich hoat: chi cho may PENDING (khac -> 409/122), huy ma cu chua dung roi phat ma moi. */
class ActivationCodeServiceReissueTest {

    private DeviceRepository deviceRepository;
    private ActivationCodeRepository activationCodeRepository;
    private ActivationCodeService service;

    @BeforeEach
    void setUp() {
        deviceRepository = mock(DeviceRepository.class);
        activationCodeRepository = mock(ActivationCodeRepository.class);
        service = new ActivationCodeService(deviceRepository, activationCodeRepository);
    }

    @Test
    void rejectsActiveDevice() {
        when(deviceRepository.findById("K1")).thenReturn(Optional.of(device(DeviceEntity.STATUS_ACTIVE)));
        ApiException ex = assertThrows(ApiException.class, () -> service.reissueActivationCode("K1"));
        assertEquals(HttpStatus.CONFLICT, ex.getHttpStatus());
        assertEquals("122", ex.getCode());
        verify(activationCodeRepository, never()).save(any());
    }

    @Test
    void rejectsRevokedDevice() {
        when(deviceRepository.findById("K1")).thenReturn(Optional.of(device(DeviceEntity.STATUS_REVOKED)));
        ApiException ex = assertThrows(ApiException.class, () -> service.reissueActivationCode("K1"));
        assertEquals(HttpStatus.CONFLICT, ex.getHttpStatus());
        verify(activationCodeRepository, never()).save(any());
    }

    @Test
    void missingDeviceIs404() {
        when(deviceRepository.findById("K1")).thenReturn(Optional.empty());
        ApiException ex = assertThrows(ApiException.class, () -> service.reissueActivationCode("K1"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getHttpStatus());
    }

    @Test
    void pendingDeviceRevokesOldCodesAndIssuesNewOne() {
        when(deviceRepository.findById("K1")).thenReturn(Optional.of(device(DeviceEntity.STATUS_PENDING)));
        ActivationCodeEntity old = new ActivationCodeEntity();
        old.setId("old");
        old.setDeviceCode("K1");
        when(activationCodeRepository.findByDeviceCodeAndUsedFalseAndRevokedFalse("K1")).thenReturn(List.of(old));

        ActivationCodeService.IssuedActivationCode issued = service.reissueActivationCode("K1");

        assertTrue(old.isRevoked());
        assertEquals("K1", issued.deviceCode());
        assertTrue(issued.activationCode().matches("[0123456789ABCDEFGHJKMNPQRSTVWXYZ]{12}"));
        verify(activationCodeRepository).save(argThat(e -> !e.getId().equals("old")
                && e.getCodeHash().equals(HashUtil.sha256Hex(issued.activationCode()))
                && !e.isRevoked() && !e.isUsed()));
    }

    private static DeviceEntity device(String status) {
        DeviceEntity d = new DeviceEntity();
        d.setDeviceCode("K1");
        d.setStoreId(1L);
        d.setStatus(status);
        return d;
    }
}
