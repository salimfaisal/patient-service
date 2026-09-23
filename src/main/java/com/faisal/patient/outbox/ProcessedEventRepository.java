package com.faisal.patient.outbox;

import com.faisal.patient.event.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProcessedEventRepository
        extends JpaRepository<ProcessedEventEntity, String> {

    @Modifying
    @Query(value = """
        INSERT INTO processed_events (event_id)
        VALUES (:eventId)
        ON CONFLICT (event_id) DO NOTHING
        """, nativeQuery = true)
    int recordIfNew(@Param("eventId") String eventId);
}