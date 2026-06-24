package cn.jieyuc.user.infrastructure.web;

import cn.jieyuc.user.infrastructure.context.UserActionContext;
import cn.jieyuc.user.infrastructure.context.UserActionContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class UserActionContextFilter extends OncePerRequestFilter {
    private static final String UNKNOWN = "unknown";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            UserActionContextHolder.set(new UserActionContext(resolveClientIp(request), request.getHeader("User-Agent")));
            filterChain.doFilter(request, response);
        } finally {
            UserActionContextHolder.clear();
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String clientIp = firstValidIp(request.getHeader("X-Forwarded-For"));
        if (hasText(clientIp)) {
            return clientIp;
        }
        clientIp = firstValidIp(request.getHeader("X-Real-IP"));
        if (hasText(clientIp)) {
            return clientIp;
        }
        clientIp = firstValidIp(request.getHeader("Proxy-Client-IP"));
        if (hasText(clientIp)) {
            return clientIp;
        }
        clientIp = firstValidIp(request.getHeader("WL-Proxy-Client-IP"));
        if (hasText(clientIp)) {
            return clientIp;
        }
        return request.getRemoteAddr();
    }

    private String firstValidIp(String value) {
        if (!hasText(value)) {
            return null;
        }
        String first = value.split(",")[0].trim();
        return UNKNOWN.equalsIgnoreCase(first) ? null : first;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
