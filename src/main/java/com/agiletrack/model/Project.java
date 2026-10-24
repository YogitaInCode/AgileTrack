package com.agiletrack.model;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity public class Project {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public String name;
 @Column(length=2000) public String description;
 public String owner; public String status; public LocalDate startDate; public LocalDate endDate;
 public Project() {}
}