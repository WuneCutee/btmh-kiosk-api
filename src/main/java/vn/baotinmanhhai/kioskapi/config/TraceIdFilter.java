package vn.baotinmanhhai.kioskapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.baotinmanhhai.kioskapi.common.ApiResponse;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * X-Trace-Id la header BAT BUOC tren moi request (tru cac file tinh/actuator noi bo).
 * Thieu hoac sai dinh dang UUID -> 400 NGAY, truoc moi xu ly khac; server luon echo lai dung gia
 * tri nay trong header response va dung lam correlation id trong log qua MDC.
 *
 * Dat @Order gan nhat RE_SOLVE de day la filter dau tien chay (truoc OpsAuthFilter, DeviceAuthFilter...).
 */
@Component
@Order(Integer.MIN_VALUE)
public class TraceIdFilter extends OncePerRequestFilter {

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    private static final String HEADER = "X-Trace-Id";

    private final ObjectMapper objectMapper;

    public TraceIdFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(HEADER);
        if (traceId == null || traceId.isBlank()) {
            writeError(response, "400", "Missing header: X-Trace-Id");
            return;
        }
        if (!UUID_PATTERN.matcher(traceId).matches()) {
            writeError(response, "400", "Invalid header: X-Trace-Id");
            return;
        }
        try {
            MDC.put("traceId", traceId);
            request.setAttribute("traceId", traceId);
            response.setHeader(HEADER, traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("traceId");
        }
    }

    private void writeError(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(code, message, null)));
    }

    // lam viec voi UUID de validation nhat quan voi cach sinh UUID chuan (phong truong hop
    // UUID_PATTERN chat hon can thiet trong tuong lai)
    private static boolean isValidUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
