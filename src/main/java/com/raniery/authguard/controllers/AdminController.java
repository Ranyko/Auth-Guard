package com.raniery.authguard.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de área restrita da aplicação.
 * 
 * Os endpoints desta classe estão protegidos pelo SecurityConfig e só podem 
 * ser acessados por usuários que possuam a permissão ROLE_ADMIN.
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/painel")
    public ResponseEntity<String> adminPanel() {
        return ResponseEntity.ok("Bem-vindo ao Painel Secreto do Administrador! Só você tem a chave :)");
    }
}
