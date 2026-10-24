package com.agiletrack.model;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity public class Sprint {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public String name; public Long projectId; public LocalDate startDate; public LocalDate endDate;
 public String status; public Integer goalPoints;
 public Sprint() {}
}