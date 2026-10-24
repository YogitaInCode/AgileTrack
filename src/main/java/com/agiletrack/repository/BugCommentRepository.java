package com.agiletrack.repository;
import com.agiletrack.model.BugComment; import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface BugCommentRepository extends JpaRepository<BugComment,Long>{ List<BugComment> findByBugIdOrderByCreatedAtAsc(Long bugId); }