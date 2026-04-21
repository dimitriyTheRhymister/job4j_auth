package ru.job4j.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import ru.job4j.repository.PersonRepository;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static ru.job4j.filter.JWTAuthenticationFilter.*;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

    private final PersonRepository personRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JWTAuthorizationFilter(AuthenticationManager authManager,
                                  PersonRepository personRepository) {
        super(authManager);
        this.personRepository = personRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        String header = request.getHeader(HEADER_STRING);

        /* Если нет токена - пропускаем (может быть публичный эндпоинт) */
        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                chain.doFilter(request, response);
            } else {
                /* Если аутентификация не удалась - возвращаем ошибку напрямую */
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid token: user not found or token expired", request.getRequestURI());
            }
        } catch (Exception e) {
            /* При любой ошибке - возвращаем ошибку напрямую */
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid or expired token: " + e.getMessage(), request.getRequestURI());
        }
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message, String path) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", message);
        errorResponse.put("status", status.value());
        errorResponse.put("path", path);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);

        if (token != null) {
            try {
                String user = JWT.require(Algorithm.HMAC512(SECRET.getBytes()))
                        .build()
                        .verify(token.replace(TOKEN_PREFIX, ""))
                        .getSubject();

                if (user != null && personRepository.findByLogin(user).isPresent()) {
                    return new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
                }
            } catch (Exception e) {
                /* Логируем ошибку валидации токена */
                System.out.println("Token validation error: " + e.getMessage());
            }
        }
        return null;
    }
}