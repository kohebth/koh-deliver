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
}
