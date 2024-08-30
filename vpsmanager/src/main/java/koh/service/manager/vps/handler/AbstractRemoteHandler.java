package koh.service.manager.vps.handler;

import koh.service.manager.vps.kafka.KafkaProducerWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Slf4j
public abstract class AbstractRemoteHandler implements MessageHandler {
    final KafkaProducerWorker bus;
    AbstractRemoteHandler(KafkaProducerWorker bus) {
        this.bus = bus;
    }

    @Override
    public final void handle(ConsumerRecord<String, String> rawMessage) {
        try {
            if (accept(rawMessage)) {
                log.info("Handled message {}", rawMessage);
            } else {
                log.warn("Rejected message {}", rawMessage);
            }
        } catch (Exception e) {
            log.error("Caught {} while handling message {}", e.getStackTrace(), rawMessage);
        }
    }

    protected abstract boolean accept(ConsumerRecord<String, String> rawMessage)
            throws Exception;
}
