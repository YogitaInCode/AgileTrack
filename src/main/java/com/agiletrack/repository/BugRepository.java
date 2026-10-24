package com.agiletrack.repository;
import com.agiletrack.model.Bug; import org.springframework.data.jpa.repository.JpaRepository;
public interface BugRepository extends JpaRepository<Bug,Long>{}