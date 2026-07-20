package com.raniery.authguard.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.raniery.authguard.models.User;
import com.raniery.authguard.repositories.UserRepository;
import com.raniery.authguard.services.TokenService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro de segurança customizado que intercepta todas as requisições HTTP.
 * 
 * Este filtro extrai o token JWT do cabeçalho "Authorization", valida a sua
 * assinatura e expiração através do {@link TokenService} e, caso seja válido,
 * injeta a identidade do usuário (e seus Cargos/Roles) no SecurityContext
 * do Spring, permitindo que a aplicação saiba quem está logado.
 */
@Component
public class SecurityFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, java.io.IOException {

        var token = this.recoverToken(request);

        if (token != null) {
            var email = tokenService.validateToken(token);

            if (!email.isEmpty()) {

                User user = userRepository.findByEmail(email);

                if (user != null) {
                    var authorities = user.getRoles().stream()
                            .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                    role.getName()))
                            .toList();

                    var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    logger.debug("Usuário autenticado via filtro: {}", user.getEmail());
                    logger.debug("Roles encontradas: {}", authorities);
                } else {
                    logger.warn("Token válido para email que não existe mais na base: {}", email);
                }

            }
        }

        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");

        if (authHeader == null)
            return null;

        return authHeader.replace("Bearer ", "");
    }

}
