package koh.service.remote.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.List;

@Slf4j
public abstract class AbstractRemoteHandler implements MessageHandler {
    final Producer<String, String> producer;
    final Consumer<String, String> consumer;

    AbstractRemoteHandler(Producer<String, String> producer, Consumer<String, String> consumer) {
        this.producer = producer;
        this.consumer = consumer;
        this.consumer.subscribe(List.of(this.topic()));
    }

    @Override
    public final void handle(ConsumerRecord<String, String> rawMessage) {
        try {
            if (accept(rawMessage)) {
                ProducerRecord<String, String> forward = produce();
                this.producer.send(forward);
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

    protected abstract ProducerRecord<String, String> produce()
            throws Exception;

    protected abstract String topic();
}
