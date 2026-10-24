package com.agiletrack.model;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity @Table(name="user_accounts")
public class UserAccount {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false, unique=true) public String email;
    @Column(nullable=false) public String fullName;
    @JsonIgnore @Column(nullable=false) public String passwordHash;
    @Column(nullable=false) public String role;
    public String department;
    public boolean active=true;
    public UserAccount() {}
}