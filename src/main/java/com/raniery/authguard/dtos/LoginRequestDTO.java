package com.raniery.authguard.dtos;

/**
 * DTO (Data Transfer Object) responsável por receber os dados de Login do cliente.
 */
public record LoginRequestDTO(String email, String password) {
    
}
