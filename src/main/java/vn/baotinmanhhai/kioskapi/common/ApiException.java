package vn.baotinmanhhai.kioskapi.common;

import org.springframework.http.HttpStatus;

/**
 * Loai exception chung cho moi loi nghiep vu/xac thuc co the dien ta bang {httpStatus, code,
 * message, data} theo bang ma loi chung (II.4) hoac ma rieng cua tung API. GlobalExceptionHandler
 * bat loai nay va render dung ApiResponse, KHONG lo chi tiet stacktrace ra ngoai.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String code;
    private final Object data;
    private final Integer retryAfterSeconds;

    public ApiException(HttpStatus httpStatus, String code, String message) {
        this(httpStatus, code, message, null, null);
    }

    public ApiException(HttpStatus httpStatus, String code, String message, Object data) {
        this(httpStatus, code, message, data, null);
    }

    public ApiException(HttpStatus httpStatus, String code, String message, Object data, Integer retryAfterSeconds) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
        this.data = data;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public Object getData() {
        return data;
    }

    public Integer getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
