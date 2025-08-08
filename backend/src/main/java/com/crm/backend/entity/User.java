package com.crm.backend.entity;

import java.util.Date;

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

    @Column(name = "User_Name")
    private String userName;

    @Column(name = "User_Email")
    private String userEmail;

    @Column(name= "User_Role")
    private String userRole;

    @Column(name= "Department")
    private String Department;

    @Column(name= "Created_At")
    private Date createdAt;

    @Column(name= "Updated_At")
    private Date updatedAt;

}
