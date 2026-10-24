package com.agiletrack.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="reported_bugs") public class Bug {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public Long projectId; @Column(nullable=false) public String title;
 @Column(length=3000) public String description;
 public String severity; public String priority; public String status;
 public String reporter; public String assignee; public LocalDateTime createdAt;
 public LocalDateTime updatedAt;
 public Bug() {}
 @PrePersist public void onCreate(){createdAt=LocalDateTime.now();updatedAt=createdAt;}
 @PreUpdate public void onUpdate(){updatedAt=LocalDateTime.now();}
}