package com.raniery.authguard.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.raniery.authguard.models.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {

    Role findByName (String name);
    
}
