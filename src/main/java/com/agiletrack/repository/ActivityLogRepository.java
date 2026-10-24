package com.agiletrack.repository;
import com.agiletrack.model.ActivityLog; import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ActivityLogRepository extends JpaRepository<ActivityLog,Long>{ List<ActivityLog> findTop20ByOrderByCreatedAtDesc(); }