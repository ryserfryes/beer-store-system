package com.beerstoresystem.order.messaging

import com.beerstoresystem.order.domain.repository.OrderOutboxEventRepository
import com.beerstoresystem.order.domain.model.OrderOutboxEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Component
class OutboxPublisher(
    private val outboxEventRepository: OrderOutboxEventRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    private val log = LoggerFactory.getLogger(OutboxPublisher::class.java)

    @Scheduled(fixedDelayString = "\${outbox.scheduler.fixedDelay:5000}")
    @Transactional
    fun publishPendingEvents() {
        val events = outboxEventRepository.findUnpublished()
        for (event in events) {
            try {
                val topic = eventTypeToTopic(event.eventType)
                kafkaTemplate.send(topic, event.aggregateId, event.payload).get()
                outboxEventRepository.save(event.copy(publishedAt = OffsetDateTime.now()))
                log.info("Published outbox event {} to topic {}", event.id, topic)
            } catch (e: Exception) {
                log.error("Failed to publish outbox event {}: {}", event.id, e.message)
            }
        }
    }

    private fun eventTypeToTopic(eventType: String): String = when (eventType) {
        "ORDER_PLACED" -> "orders.placed"
        "ORDER_PICKED_UP" -> "orders.picked-up"
        else -> eventType.lowercase().replace("_", "-")
    }
}
