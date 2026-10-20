package vn.baotinmanhhai.kioskapi.catalog.adminclient;

/** admin-portal (nguon catalog duy nhat) khong toi duoc hoac tra loi khong hop le. */
public class AdminPortalUnavailableException extends RuntimeException {
    public AdminPortalUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
