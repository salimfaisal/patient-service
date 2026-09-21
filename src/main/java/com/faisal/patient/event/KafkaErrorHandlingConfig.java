package com.faisal.patient.event;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandlingConfig {

    @Bean
    CommonErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, PatientCreatedEvent> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) ->
                                new TopicPartition(
                                        record.topic() + ".DLT",
                                        record.partition()));

        // 1-second delay, 2 retries = 3 processing attempts total
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1_000L, 2L));
    }
}