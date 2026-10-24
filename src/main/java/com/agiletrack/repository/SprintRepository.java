package com.agiletrack.repository;
import com.agiletrack.model.Sprint; import org.springframework.data.jpa.repository.JpaRepository;
public interface SprintRepository extends JpaRepository<Sprint,Long>{}