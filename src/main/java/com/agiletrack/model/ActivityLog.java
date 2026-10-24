package com.agiletrack.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity public class ActivityLog {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public String actor; public String action; public String entityType; public Long entityId;
 public LocalDateTime createdAt;
 public ActivityLog() {}
 @PrePersist public void createTime(){createdAt=LocalDateTime.now();}
}