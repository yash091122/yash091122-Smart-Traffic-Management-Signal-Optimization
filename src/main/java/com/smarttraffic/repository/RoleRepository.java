package com.smarttraffic.repository;

import com.smarttraffic.entity.Role;
import com.smarttraffic.entity.Role.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
