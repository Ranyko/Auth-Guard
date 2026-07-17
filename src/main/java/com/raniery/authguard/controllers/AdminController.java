package com.raniery.authguard.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/painel")
    public ResponseEntity<String> adminPanel() {
        return ResponseEntity.ok("Bem-vindo ao Painel Secreto do Administrador! Só você tem a chave :)");
    }
}
