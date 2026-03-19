package com.example.api.JWT;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String auth = req.getHeader("Authorization");

        if (auth != null) {
            if (!auth.startsWith("Bearer ")) {
                req.setAttribute("jwt_error", "Authorization 헤더 형식이 올바르지 않습니다.");
                SecurityContextHolder.clearContext();
                chain.doFilter(req, res);
                return;
            }

            String token = auth.substring(7).trim();

            if (token.isEmpty()) {
                req.setAttribute("jwt_error", "access token이 비어 있습니다.");
                SecurityContextHolder.clearContext();
                chain.doFilter(req, res);
                return;
            }

            try {
                String userId = jwtProvider.validateAndGetSubject(token);

                var authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, List.of());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (ExpiredJwtException e) {
                SecurityContextHolder.clearContext();
                req.setAttribute("jwt_error", "만료된 access token입니다.");
                log.warn("Expired JWT: {}", e.getMessage());

            } catch (MalformedJwtException e) {
                SecurityContextHolder.clearContext();
                req.setAttribute("jwt_error", "잘못된 형식의 access token입니다.");
                log.warn("Malformed JWT: {}", e.getMessage());

            } catch (SecurityException e) {
                SecurityContextHolder.clearContext();
                req.setAttribute("jwt_error", "유효하지 않은 access token입니다.");
                log.warn("Invalid JWT signature: {}", e.getMessage());

            } catch (UnsupportedJwtException e) {
                SecurityContextHolder.clearContext();
                req.setAttribute("jwt_error", "지원되지 않는 access token입니다.");
                log.warn("Unsupported JWT: {}", e.getMessage());

            } catch (IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
                req.setAttribute("jwt_error", "access token 값이 올바르지 않습니다.");
                log.warn("Illegal JWT argument: {}", e.getMessage());

            } catch (JwtException e) {
                SecurityContextHolder.clearContext();
                req.setAttribute("jwt_error", "유효하지 않은 access token입니다.");
                log.warn("JWT exception: {}", e.getMessage());

            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                req.setAttribute("jwt_error", "access token 처리 중 서버 오류가 발생했습니다.");
                log.error("JWT processing error", e);
            }
        }

        chain.doFilter(req, res);
    }
}