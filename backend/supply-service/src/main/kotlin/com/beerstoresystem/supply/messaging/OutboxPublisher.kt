package com.beerstoresystem.supply.messaging

import com.beerstoresystem.supply.domain.repository.OutboxEventRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Component
class OutboxPublisher(
    private val outboxEventRepository: OutboxEventRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>
) {

    private val log = LoggerFactory.getLogger(OutboxPublisher::class.java)

    @Scheduled(fixedDelayString = "\${outbox.scheduler.fixedDelay:1000}")
    @Transactional
    fun publishPendingEvents() {
        val events = outboxEventRepository.findTop50PendingOrderedByCreatedAt()
        if (events.isEmpty()) return

        for (event in events) {
            try {
                kafkaTemplate.send(event.eventType, event.aggregateId, event.payload).get()
                val published = event.copy(publishedAt = OffsetDateTime.now())
                outboxEventRepository.save(published)
                log.info("Published outbox event {} to topic {}", event.id, event.eventType)
            } catch (ex: Exception) {
                log.error("Failed to publish outbox event {}: {}", event.id, ex.message)
            }
        }
    }
}
