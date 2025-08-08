package com.crm.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="administrator_data")
public class Administrator {
    
    @Id
    private Long userId;

    @OneToOne
    private String userName;

    private String userEmail;

    private String userRole; 
}
