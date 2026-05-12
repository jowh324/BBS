package com.example.api.Service;

import com.example.api.DTO.JetsonDTOs;
import com.example.api.Entity.JetsonLog;
import com.example.api.Repository.JetsonLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JetsonLogService {

    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 200;

    private final JetsonLogRepository jetsonLogRepository;

    @Transactional
    public JetsonDTOs.LogResponse appendLog(JetsonDTOs.LogRequest req) {
        JetsonLog log = new JetsonLog();
        log.setId(UUID.randomUUID().toString());
        log.setMessage(req.message().trim());
        log.setCreatedAt(Instant.now());
        jetsonLogRepository.save(log);
        return toResponse(log);
    }

    @Transactional(readOnly = true)
    public List<JetsonDTOs.LogResponse> listLogs(Instant after, Integer limit) {
        int size = normalizeLimit(limit);
        List<JetsonLog> logs;

        if (after == null) {
            logs = jetsonLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, size));
            Collections.reverse(logs);
        } else {
            logs = jetsonLogRepository.findByCreatedAtAfterOrderByCreatedAtAsc(after, PageRequest.of(0, size));
        }

        return logs.stream()
                .map(this::toResponse)
                .toList();
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < 1) {
            return 1;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private JetsonDTOs.LogResponse toResponse(JetsonLog log) {
        return new JetsonDTOs.LogResponse(
                log.getId(),
                log.getMessage(),
                log.getCreatedAt()
        );
    }
}
