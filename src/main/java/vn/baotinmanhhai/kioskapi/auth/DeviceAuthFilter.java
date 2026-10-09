package vn.baotinmanhhai.kioskapi.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.baotinmanhhai.kioskapi.catalog.service.ChannelConfigService;
import vn.baotinmanhhai.kioskapi.common.ApiResponse;
import vn.baotinmanhhai.kioskapi.device.repo.DeviceRepository;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Kiem Bearer accessToken tren moi API tru 3 API xac thuc thiet bi (II.2.1/II.2.14/II.2.15),
 * /internal/ops/** (co co che rieng - xem OpsAuthFilter) va /actuator/**.
 *
 * Thu tu kiem (II.5.3): chu ky HS256 + exp (+-60s) + iss/tokenType/partnerCode dung -> sai bat
 * ky dieu kien nao -> 401; sub khac X-Device-Code -> 401; trang thai may REVOKED -> 403/121 (ap
 * dung ngay, moi API); kenh KIOSK tam dung -> 403/201 (CHI voi cac API du lieu II.2.2-II.2.11 va
 * II.3 - KHONG ap cho II.2.12/II.2.13 du van qua filter nay).
 */
@Component
@Order(-100)
public class DeviceAuthFilter extends OncePerRequestFilter {

    private static final Set<String> EXCLUDED_PREFIXES = Set.of(
            "/api/v1/kiosk/device-sessions",
            "/api/v1/kiosk/devices/enroll",
            "/api/v1/kiosk/devices/rotate-key",
            "/internal/",
            "/actuator"
    );

    // Cac duong dan phai hoat dong binh thuong KE CA khi kenh KIOSK tam dung (II.4 code 201).
    private static final Set<String> CHANNEL_PAUSE_EXEMPT = Set.of(
            "/api/v1/kiosk/bootstrap",
            "/api/v1/kiosk/errors"
    );

    private final JwtService jwtService;
    private final DeviceRepository deviceRepository;
    private final ChannelConfigService channelConfigService;
    private final ObjectMapper objectMapper;

    public DeviceAuthFilter(JwtService jwtService, DeviceRepository deviceRepository,
                             ChannelConfigService channelConfigService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.deviceRepository = deviceRepository;
        this.channelConfigService = channelConfigService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDED_PREFIXES.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "401", "Unauthorized or invalid token");
            return;
        }
        String token = authHeader.substring("Bearer ".length()).trim();
        JwtService.VerifiedToken verified = jwtService.verify(token);
        if (verified == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "401", "Unauthorized or invalid token");
            return;
        }
        String headerDeviceCode = request.getHeader("X-Device-Code");
        if (headerDeviceCode == null || !headerDeviceCode.equals(verified.deviceCode())) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "401", "Unauthorized or invalid token");
            return;
        }

        var deviceOpt = deviceRepository.findById(verified.deviceCode());
        if (deviceOpt.isEmpty() || deviceOpt.get().isRevoked()) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "121", "May da bi thu hoi");
            return;
        }

        String path = request.getRequestURI();
        boolean exemptFromPause = CHANNEL_PAUSE_EXEMPT.stream().anyMatch(path::equals);
        if (!exemptFromPause && channelConfigService.isChannelPaused()) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "201", "Kenh KIOSK dang tam dung");
            return;
        }

        request.setAttribute("deviceCode", verified.deviceCode());
        request.setAttribute("storeId", verified.storeId());
        request.setAttribute("keyId", verified.keyId());

        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(code, message, null)));
    }
}
