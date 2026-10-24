package com.agiletrack.repository;
import com.agiletrack.model.TestCase; import org.springframework.data.jpa.repository.JpaRepository;
public interface TestCaseRepository extends JpaRepository<TestCase,Long>{}