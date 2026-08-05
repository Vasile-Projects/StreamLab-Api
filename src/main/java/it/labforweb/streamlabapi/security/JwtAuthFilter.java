package it.labforweb.streamlabapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    // path pubblici: qui non serve un access token valido, l'utente non lo ha o lo sta rinnovando

    private static final List<String> PATH_PUBBLICI = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
            "/api/v1/abbonamenti"
    );

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // le richieste di preflight CORS non hanno mai un token, e non devono passare
        // dal controllo di autenticazione — altrimenti bloccano la risposta CORS stessa
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }


        String path = request.getRequestURI();

        // endpoint pubblico: nessun controllo, si passa direttamente al controller

        if (PATH_PUBBLICI.contains(path) || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        Cookie[] cookies = request.getCookies();

        if(cookies == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String accessJwt = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("accessToken"))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);


        if(accessJwt == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try{
            Claims claims = jwtService.parseAndValidate(accessJwt);
            request.setAttribute("userId", claims.get(JwtService.CLAIM_USER_ID, Integer.class));
            request.setAttribute("email", claims.getSubject());
        } catch (JwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}