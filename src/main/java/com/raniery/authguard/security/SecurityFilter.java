package com.raniery.authguard.security;

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

@Component
public class SecurityFilter extends OncePerRequestFilter{
    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, java.io.IOException{
        
        var token = this.recoverToken(request);
        
        if (token != null){
            var email = tokenService.validateToken(token);

            if (!email.isEmpty()){
                User user = userRepository.findByEmail(email);

                var authorities = user.getRoles().stream().map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(role.getName())).toList();

                var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                System.out.println("USUÁRIO PASSOU NO FILTRO: " + user.getEmail());
                System.out.println("ROLES ENCONTRADAS: " + authorities);
        
            }
        }

        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request){
        var authHeader = request.getHeader("Authorization");
        
        if (authHeader == null) return null;

        return authHeader.replace("Bearer ", "");
    }
    
}
