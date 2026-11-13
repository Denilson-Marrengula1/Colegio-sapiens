package mz.colegiosapi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Set;

/** Exige um cabeçalho que um site de outra origem não pode enviar sem autorização CORS. */
final class RequestOriginProtectionFilter extends OncePerRequestFilter {
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        if (!SAFE_METHODS.contains(request.getMethod())
                && !"XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Cabeçalho de protecção obrigatório");
            return;
        }
        chain.doFilter(request, response);
    }
}
