package pe.upc.equilibria.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class AppConfig {

    @Value("${app.cors.allowed-origins}") private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration cfg = new CorsConfiguration();
        Arrays.stream(allowedOrigins.split(",")).map(String::trim).forEach(cfg::addAllowedOrigin);
        cfg.addAllowedHeader("*");
        cfg.addAllowedMethod("*");
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return new CorsFilter(src);
    }

    @Bean
    public Filter rateLimitFilter(
            @Value("${app.rate-limit.capacity}") int capacity,
            @Value("${app.rate-limit.refill-minutes}") int refillMinutes) {
        return new RateLimitFilter(capacity, refillMinutes);
    }
}

class RateLimitFilter implements Filter {
    private final int capacity;
    private final long refillMs;
    private final Map<String, AtomicInteger> counts = new ConcurrentHashMap<>();
    private final Map<String, Long> resetTimes = new ConcurrentHashMap<>();

    RateLimitFilter(int capacity, int refillMinutes) {
        this.capacity = capacity;
        this.refillMs = (long) refillMinutes * 60 * 1000;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        String ip = ((HttpServletRequest) req).getRemoteAddr();
        long now = System.currentTimeMillis();
        resetTimes.merge(ip, now + refillMs, (old, v) -> old < now ? v : old);
        if (resetTimes.get(ip) <= now) {
            counts.remove(ip);
            resetTimes.put(ip, now + refillMs);
        }
        int c = counts.computeIfAbsent(ip, k -> new AtomicInteger(0)).incrementAndGet();
        if (c > capacity) {
            ((HttpServletResponse) res).setStatus(429);
            res.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
            return;
        }
        chain.doFilter(req, res);
    }
}
