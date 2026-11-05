package in.wb.nhls.config;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 401/403-ও GlobalExceptionHandler-এর মতো একই JSON format-এ যাবে
@Component
public class JsonSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException ex) throws IOException {

        String message = ex instanceof InvalidBearerTokenException
                ? "Token অবৈধ অথবা মেয়াদ শেষ, আবার login করুন"
                : "অনুগ্রহ করে লগইন করুন";

        response.setHeader("WWW-Authenticate", "Bearer");
        write(response, HttpStatus.UNAUTHORIZED, message);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException ex) throws IOException {

        write(response, HttpStatus.FORBIDDEN, "এই কাজ করার অনুমতি আপনার নেই");
    }

    private static void write(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"success\":false,\"message\":\"" + message + "\",\"data\":null,\"pagination\":null}");
    }
}
