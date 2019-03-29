package com.thm.app_server.repository;

import com.thm.app_server.model.Role;
import com.thm.app_server.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.Max;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName roleName);
    List<Role> findByNameIn(RoleName[] roleNames);
}