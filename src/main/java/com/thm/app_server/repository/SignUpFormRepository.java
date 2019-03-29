package com.thm.app_server.repository;

import com.thm.app_server.model.SignUpForm;
import com.thm.app_server.model.SignUpFormStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignUpFormRepository extends JpaRepository<SignUpForm, Long> {
    List<SignUpForm> findAllByStatus(SignUpFormStatus status);
}
