package vn.baotinmanhhai.kioskapi.ops;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.baotinmanhhai.kioskapi.common.ApiResponse;

import java.io.IOException;
import java.security.MessageDigest;

/**
 * KHONG thuoc tai lieu tich hap API Kiosk. Day la "seam" van hanh noi bo CHI de bring-up/test
 * standalone du an nay (tao may PENDING + lay ma kich hoat dau tien, thu hoi may) vi du an khong
 * co admin UI rieng. Bao ve bang pre-shared header X-Ops-Key, KHONG dung JWT.
 */
@Component
@Order(-50)
public class OpsAuthFilter extends OncePerRequestFilter {

    private final String opsApiKey;
    private final ObjectMapper objectMapper;

    public OpsAuthFilter(@Value("${kiosk.ops.api-key}") String opsApiKey, ObjectMapper objectMapper) {
        this.opsApiKey = opsApiKey;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal/ops/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String provided = request.getHeader("X-Ops-Key");
        if (provided == null || !constantTimeEquals(provided, opsApiKey)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(
                    ApiResponse.error("403", "Invalid or missing X-Ops-Key", null)));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(Utf8.encode(a), Utf8.encode(b));
    }
}
