package com.raniery.authguard.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.raniery.authguard.models.User;

import java.time.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Serviço responsável por toda a criptografia e validação de Tokens JWT.
 * Utiliza o algoritmo HMAC256 e uma chave secreta (definida nas variáveis de ambiente)
 * para garantir que os tokens não foram forjados ou adulterados.
 */
@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(User user){
        try{
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.create()
                    .withIssuer("authguard-api")
                    .withSubject(user.getEmail())
                    .withExpiresAt(generateExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception){
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    private Instant generateExpirationDate(){
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }


    public String validateToken(String token){
        try{

            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.require(algorithm).withIssuer("authguard-api").build().verify(token).getSubject();
        } catch (JWTVerificationException exception){
            logger.warn("ERRO NA VALIDAÇÃO DO TOKEN:{}", exception.getMessage());
            return "";
        }
    }
    
}
