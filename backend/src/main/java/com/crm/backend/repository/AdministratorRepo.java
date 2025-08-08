package com.crm.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.crm.backend.entity.Administrator;

public interface AdministratorRepo extends JpaRepository<Administrator, Long>{
    
}
