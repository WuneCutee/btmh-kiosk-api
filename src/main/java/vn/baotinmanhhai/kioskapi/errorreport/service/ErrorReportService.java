package vn.baotinmanhhai.kioskapi.errorreport.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.baotinmanhhai.kioskapi.common.ErrorCodes;
import vn.baotinmanhhai.kioskapi.common.ValidationErrorItem;
import vn.baotinmanhhai.kioskapi.device.entity.DeviceErrorLogEntity;
import vn.baotinmanhhai.kioskapi.device.repo.DeviceErrorLogRepository;
import vn.baotinmanhhai.kioskapi.errorreport.dto.ErrorItemDto;
import vn.baotinmanhhai.kioskapi.errorreport.dto.ErrorReportRequest;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** II.2.13. */
@Service
public class ErrorReportService {

    private static final Logger log = LoggerFactory.getLogger(ErrorReportService.class);
    private static final int MAX_CONTEXT_BYTES = 2048;

    private final DeviceErrorLogRepository repository;
    private final ObjectMapper objectMapper;

    public ErrorReportService(DeviceErrorLogRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /**
     * Hai buoc tach biet (khong luu xen ke): (1) kiem TOAN BO errors[] (kich thuoc context),
     * gop het loi vao 1 danh sach va nem 400 MOT LAN neu co bat ky loi nao; (2) chi sau khi ca lo
     * da qua kiem tra het moi ghi DB. Tranh truong hop ghi mot phan (vd errors[0] da luu) roi
     * errors[2] moi phat hien loi 400 - response bao loi nhung du lieu da bi ghi mot phan.
     */
    @Transactional
    public void report(String deviceCode, ErrorReportRequest request) {
        List<ErrorItemDto> items = request.getErrors();
        List<String> contextJsons = new ArrayList<>(items.size());
        List<ValidationErrorItem> fieldErrors = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            try {
                contextJsons.add(serializeAndCheckContext(items.get(i)));
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                throw ErrorCodes.malformedBody();
            } catch (ContextTooLongException e) {
                fieldErrors.add(new ValidationErrorItem("errors[" + i + "].context", ValidationErrorItem.TOO_LONG));
                contextJsons.add(null);
            }
        }
        if (!fieldErrors.isEmpty()) {
            throw ErrorCodes.validationError(fieldErrors);
        }

        for (int i = 0; i < items.size(); i++) {
            ErrorItemDto item = items.get(i);
            if (repository.existsByDeviceCodeAndErrorId(deviceCode, item.getId())) {
                // Da nhan truoc do (dedup theo deviceCode + id) -> van coi la thanh cong, bo qua.
                continue;
            }
            DeviceErrorLogEntity entity = new DeviceErrorLogEntity();
            entity.setId(UUID.randomUUID().toString());
            entity.setDeviceCode(deviceCode);
            entity.setErrorId(item.getId());
            entity.setOccurredAt(item.getOccurredAt());
            entity.setReceivedAt(Instant.now());
            entity.setType(item.getType());
            entity.setMessage(item.getMessage());
            entity.setContext(contextJsons.get(i));
            repository.save(entity);

            log.warn("Loi tu may kiosk deviceCode={} type={} message={} context={}",
                    deviceCode, item.getType(), item.getMessage(), contextJsons.get(i));
        }
    }

    private String serializeAndCheckContext(ErrorItemDto item)
            throws com.fasterxml.jackson.core.JsonProcessingException, ContextTooLongException {
        if (item.getContext() == null) {
            return null;
        }
        String json = objectMapper.writeValueAsString(item.getContext());
        int bytes = json.getBytes(StandardCharsets.UTF_8).length;
        if (bytes > MAX_CONTEXT_BYTES) {
            throw new ContextTooLongException();
        }
        return json;
    }

    private static final class ContextTooLongException extends Exception {
    }
}
