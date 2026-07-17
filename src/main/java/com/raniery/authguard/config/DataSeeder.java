package com.raniery.authguard.config;


import com.raniery.authguard.models.Role;
import com.raniery.authguard.models.User;
import com.raniery.authguard.repositories.RoleRepository;
import com.raniery.authguard.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        
    
        Role adminRole = roleRepository.findByName("ROLE_ADMIN");
        if (adminRole == null) {
            adminRole = new Role("ROLE_ADMIN");
            roleRepository.save(adminRole);
        }

        
        Role userRole = roleRepository.findByName("ROLE_USER");
        if (userRole == null) {
            userRole = new Role("ROLE_USER");
            roleRepository.save(userRole);
        }


        User meuUsuario = userRepository.findByEmail("raniery3@email.com");
        
    
        if (meuUsuario != null && !meuUsuario.getRoles().contains(adminRole)) {
            meuUsuario.getRoles().add(adminRole);
            userRepository.save(meuUsuario); 
            
            System.out.printf("%s virou ADMIN", meuUsuario.getEmail());
        }
    }

    
}
