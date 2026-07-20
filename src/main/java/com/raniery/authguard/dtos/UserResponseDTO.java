package com.raniery.authguard.dtos;

/**
 * DTO de resposta para dados de usuário.
 * Expõe apenas os campos seguros para o cliente, evitando
 * que informações sensíveis (como a senha hasheada) vazem na API.
 */

public record UserResponseDTO(Long id, String name, String email) {
    
}
