package com.agiletrack.model;
import jakarta.persistence.*;
@Entity @Table(name="work_tasks") public class Task {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public Long projectId; public Long sprintId;
 @Column(nullable=false) public String title;
 @Column(length=2500) public String description;
 public String assignee; public String priority; public String status;
 public Integer storyPoints; public String taskType;
 public Task() {}
}