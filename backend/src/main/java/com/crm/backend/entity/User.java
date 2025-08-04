package com.crm.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data // it is used to create getter , setter and tostring 
@NoArgsConstructor  // it is used to create a constructor without argunments
@AllArgsConstructor   // it is used to create a constructor with aurgunments
@Entity
@Table(name= "User_Data")

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String userName;

    @Column(name = "User_Email")
    private String userEmail;

    private String userRole;

}
