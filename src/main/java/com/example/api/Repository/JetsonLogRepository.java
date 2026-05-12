package com.example.api.Repository;

import com.example.api.Entity.JetsonLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface JetsonLogRepository extends JpaRepository<JetsonLog, String> {
    List<JetsonLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<JetsonLog> findByCreatedAtAfterOrderByCreatedAtAsc(Instant createdAt, Pageable pageable);
}
