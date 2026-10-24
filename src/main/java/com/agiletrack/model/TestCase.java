package com.agiletrack.model;
import jakarta.persistence.*;
@Entity @Table(name="quality_test_cases") public class TestCase {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public Long projectId; public String title;
 @Column(length=3000) public String steps;
 @Column(length=1500) public String expectedResult;
 @Column(length=1500) public String actualResult;
 public String status; public String tester; public Long linkedBugId;
 public TestCase() {}
}