package com.faisal.patient.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventJPARepository
        extends JpaRepository<OutboxEventEntity, String> {

    List<OutboxEventEntity> findTop20ByStatusOrderByCreatedAtAsc(
            OutboxEventStatus status);
}