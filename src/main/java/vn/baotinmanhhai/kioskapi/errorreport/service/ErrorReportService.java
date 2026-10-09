package vn.baotinmanhhai.kioskapi.errorreport.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.baotinmanhhai.kioskapi.common.ErrorCodes;
import vn.baotinmanhhai.kioskapi.common.ValidationErrorItem;
import vn.baotinmanhhai.kioskapi.device.entity.DeviceErrorLogEntity;
import vn.baotinmanhhai.kioskapi.device.repo.DeviceErrorLogRepository;
import vn.baotinmanhhai.kioskapi.errorreport.dto.ErrorItemDto;
import vn.baotinmanhhai.kioskapi.errorreport.dto.ErrorReportRequest;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
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

    public void report(String deviceCode, ErrorReportRequest request) {
        for (int i = 0; i < request.getErrors().size(); i++) {
            ErrorItemDto item = request.getErrors().get(i);
            String contextJson = serializeContext(item, i);

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
            entity.setContext(contextJson);
            repository.save(entity);

            log.warn("Loi tu may kiosk deviceCode={} type={} message={} context={}",
                    deviceCode, item.getType(), item.getMessage(), contextJson);
        }
    }

    private String serializeContext(ErrorItemDto item, int index) {
        if (item.getContext() == null) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(item.getContext());
            int bytes = json.getBytes(StandardCharsets.UTF_8).length;
            if (bytes > MAX_CONTEXT_BYTES) {
                throw ErrorCodes.validationError(List.of(
                        new ValidationErrorItem("errors[" + index + "].context", ValidationErrorItem.TOO_LONG)));
            }
            return json;
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw ErrorCodes.malformedBody();
        }
    }
}
