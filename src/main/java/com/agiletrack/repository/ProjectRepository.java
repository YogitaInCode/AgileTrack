package com.agiletrack.repository;
import com.agiletrack.model.Project; import org.springframework.data.jpa.repository.JpaRepository;
public interface ProjectRepository extends JpaRepository<Project,Long>{}