package vn.baotinmanhhai.kioskapi.ops.dto;

/** Mot dong log loi may (II.2.13) cho web admin. context la chuoi JSON goc may gui len (co the null). */
public record OpsDeviceErrorLogDto(
        String id,
        String deviceCode,
        String errorId,
        String type,
        String message,
        String occurredAt,
        String receivedAt,
        String context
) {
}
