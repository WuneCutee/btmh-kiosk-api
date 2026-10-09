package vn.baotinmanhhai.kioskapi.common;

/** Mot phan tu cua data trong loi 400 VALIDATION_ERROR: {field, code}. */
public record ValidationErrorItem(String field, String code) {

    public static final String REQUIRED = "REQUIRED";
    public static final String TOO_LONG = "TOO_LONG";
    public static final String INVALID_FORMAT = "INVALID_FORMAT";
}
