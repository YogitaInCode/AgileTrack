package com.agiletrack.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity public class BugComment {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public Long bugId; public String author;
 @Column(length=2000) public String message;
 public LocalDateTime createdAt;
 public BugComment() {}
 @PrePersist public void createTime(){createdAt=LocalDateTime.now();}
}