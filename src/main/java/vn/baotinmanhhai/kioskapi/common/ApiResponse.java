package vn.baotinmanhhai.kioskapi.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Bao boc phan hoi chung cho moi API theo muc "Body Response" cua tai lieu tich hop:
 * {code, message, data, timestamp}. data bi bo qua (khong phai null) khi khong co gia tri,
 * nho @JsonInclude.Include.NON_NULL cau hinh toan cuc tren ObjectMapper (xem JacksonConfig) va
 * tren chinh class nay de an toan khi serialize truc tiep.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    public static final String CODE_SUCCESS = "000";

    private String code;
    private String message;
    private T data;
    private String timestamp;

    public ApiResponse() {
    }

    public ApiResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = TimeUtil.nowResponseTimestamp();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(CODE_SUCCESS, "success", data);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(CODE_SUCCESS, "success", null);
    }

    public static <T> ApiResponse<T> error(String code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
