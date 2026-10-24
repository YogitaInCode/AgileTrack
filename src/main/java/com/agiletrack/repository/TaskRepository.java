package com.agiletrack.repository;
import com.agiletrack.model.Task; import org.springframework.data.jpa.repository.JpaRepository;
public interface TaskRepository extends JpaRepository<Task,Long>{}