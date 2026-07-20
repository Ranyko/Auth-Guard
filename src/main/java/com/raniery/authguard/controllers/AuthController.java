package com.raniery.authguard.controllers;

import com.raniery.authguard.dtos.LoginRequestDTO;
import com.raniery.authguard.dtos.RegisterRequestDTO;
import com.raniery.authguard.models.User;
import com.raniery.authguard.services.UserService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST responsável por expor os endpoints públicos de autenticação.
 * Protegido contra abusos e injeta validações rigorosas (via DTOs) antes de 
 * permitir acesso às regras de negócio.
 */
@RestController

@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequestDTO body) {

        try {
            User newUser = new User();
            newUser.setName(body.name());
            newUser.setEmail(body.email());
            newUser.setPassword(body.password());

            User savedUser = userService.registerUser(newUser);

            return ResponseEntity.ok(savedUser);
        }

        catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO body){
        try {
            String token = userService.loginUser(body.email(), body.password());

            return ResponseEntity.ok(token);
        } catch (RuntimeException e){

            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

}
