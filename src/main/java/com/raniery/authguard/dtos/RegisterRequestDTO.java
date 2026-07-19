package com.raniery.authguard.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record RegisterRequestDTO(

    @NotBlank(message = "O nome nao pode estar vazio!!")
    String name,

    @NotBlank(message = "O email nao pode estar vazio!")
    @Email(message = "Formato do email invalido!")
    String email,

    @NotBlank(message = "A senha nao pode estar vazia!!")
    @Size(min = 6, message = "A senha deve ter no minimo 6 caracteres")
    @Pattern(regexp = "^(?=.*[!@#$%^&*(),.?\":{}|<>]).*$", message = "A senha deve conter um caractere especial")
    String password
    
){}
