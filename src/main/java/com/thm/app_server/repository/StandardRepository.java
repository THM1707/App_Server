package com.thm.app_server.repository;

import com.thm.app_server.model.Standard;
import com.thm.app_server.model.StandardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StandardRepository extends JpaRepository<Standard, Integer> {
    List<Standard> findAll();
    Standard findByType(StandardType type);
}
