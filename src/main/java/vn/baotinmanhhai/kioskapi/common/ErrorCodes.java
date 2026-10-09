package vn.baotinmanhhai.kioskapi.common;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * Noi tap trung cac ma loi dung lai nhieu lan (muc II.4 "Ma Loi Chung" va cac ma dung chung cho
 * ba API xac thuc thiet bi II.2.1/II.2.14/II.2.15). Moi phuong thuc tra ve mot ApiException da
 * dung HTTP status + code + message dung theo tai lieu.
 */
public final class ErrorCodes {

    private ErrorCodes() {
    }

    // ---- Common (II.4) ----

    public static ApiException missingTraceIdHeader() {
        return new ApiException(HttpStatus.BAD_REQUEST, "400", "Missing header: X-Trace-Id");
    }

    public static ApiException invalidTraceIdHeader() {
        return new ApiException(HttpStatus.BAD_REQUEST, "400", "Invalid header: X-Trace-Id");
    }

    public static ApiException malformedBody() {
        return new ApiException(HttpStatus.BAD_REQUEST, "400", "Request body is missing or malformed");
    }

    public static ApiException missingParameter(String name) {
        return new ApiException(HttpStatus.BAD_REQUEST, "400", "Missing parameter: " + name);
    }

    public static ApiException invalidParameterType(String name) {
        return new ApiException(HttpStatus.BAD_REQUEST, "400", "Invalid parameter type: " + name);
    }

    public static ApiException validationError(List<ValidationErrorItem> items) {
        return new ApiException(HttpStatus.BAD_REQUEST, "400", "VALIDATION_ERROR", items);
    }

    public static ApiException unauthorized() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "401", "Unauthorized or invalid token");
    }

    public static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "404", "Not found");
    }

    public static ApiException methodNotAllowed(String method) {
        return new ApiException(HttpStatus.METHOD_NOT_ALLOWED, "405", "Method not allowed: " + method);
    }

    public static ApiException tooManyRequests(int retryAfterSeconds) {
        return new ApiException(HttpStatus.TOO_MANY_REQUESTS, "429",
                "Vuot gioi han so lan goi, thu lai sau", null, retryAfterSeconds);
    }

    public static ApiException internal() {
        return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "999",
                "The system is experiencing issues, please try again later.");
    }

    public static ApiException deviceRevoked() {
        return new ApiException(HttpStatus.FORBIDDEN, "121", "May da bi thu hoi");
    }

    public static ApiException channelPaused() {
        return new ApiException(HttpStatus.FORBIDDEN, "201", "Kenh KIOSK dang tam dung");
    }

    public static ApiException productGone() {
        return new ApiException(HttpStatus.NOT_FOUND, "202", "San pham khong ton tai hoac da tat cho kenh");
    }

    // ---- Device signature verification (common to II.2.1 / II.2.14 / II.2.15) ----

    public static ApiException invalidSignature() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "101", "Chu ky khong hop le");
    }

    public static ApiException clockSkew(long serverTimeMillis) {
        return new ApiException(HttpStatus.UNAUTHORIZED, "102",
                "Thoi gian lech qua 5 phut so voi gio server", Map.of("serverTime", serverTimeMillis));
    }

    public static ApiException nonceReused() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "103", "Nonce da duoc su dung");
    }

    public static ApiException keyNotRecognized() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "104", "Khoa khong duoc cong nhan cho may nay");
    }

    public static ApiException nonceCheckUnavailable() {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "191",
                "Tam thoi khong kiem tra duoc chong phat lai");
    }

    // ---- Enroll (II.2.14) ----

    public static ApiException activationCodeInvalid() {
        return new ApiException(HttpStatus.FORBIDDEN, "111", "Ma kich hoat khong dung");
    }

    public static ApiException activationCodeExpired() {
        return new ApiException(HttpStatus.FORBIDDEN, "112", "Ma kich hoat da het han");
    }

    public static ApiException activationCodeRevoked() {
        return new ApiException(HttpStatus.FORBIDDEN, "113", "Ma kich hoat khong con hieu luc");
    }

    public static ApiException deviceNotPending() {
        return new ApiException(HttpStatus.CONFLICT, "122", "May khong o trang thai cho kich hoat");
    }

    public static ApiException publicKeyMismatch() {
        return new ApiException(HttpStatus.CONFLICT, "123", "Public key khac khoa da dang ky");
    }

    // ---- Rotate key (II.2.15) ----

    public static ApiException deviceNotActive() {
        return new ApiException(HttpStatus.CONFLICT, "124", "May khong o trang thai hoat dong");
    }

    public static ApiException rotationTooSoon() {
        return new ApiException(HttpStatus.CONFLICT, "131", "Chua du 1 gio ke tu lan xoay khoa truoc");
    }

    public static ApiException rotationInProgressWithDifferentKey() {
        return new ApiException(HttpStatus.CONFLICT, "132", "Dang co luot xoay khoa voi khoa khac");
    }
}
