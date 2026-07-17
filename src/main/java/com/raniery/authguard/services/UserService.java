package com.raniery.authguard.services;

import com.raniery.authguard.models.Role;
import com.raniery.authguard.models.User;
import com.raniery.authguard.repositories.RoleRepository;
import com.raniery.authguard.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RoleRepository roleRepository;

    public User registerUser (User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new
            RuntimeException("Este e-mail ja esta em uso!");
        }
        
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        
        Role userRole = roleRepository.findByName("ROLE_USER");
        if (userRole != null){
            user.getRoles().add(userRole);
        }
        return userRepository.save(user);
    }

    public String loginUser(String email, String password){
        User user = userRepository.findByEmail(email);

        if (user == null){
            throw new RuntimeException("Usuário não encontrado!");
        }

        if (!passwordEncoder.matches(password, user.getPassword())){
            throw new RuntimeException("Senha incorreta!");
        }

        return tokenService.generateToken(user);
    }
    
}
