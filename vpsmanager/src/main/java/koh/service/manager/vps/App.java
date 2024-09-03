package koh.service.manager.vps;

import koh.db.hub.DataHub;
import koh.service.manager.vps.handler.*;
import koh.service.manager.vps.kafka.KafkaConfig;
import koh.service.manager.vps.kafka.KafkaConsumerWorker;
import koh.service.manager.vps.kafka.KafkaProducerWorker;
import koh.service.manager.vps.kafka.KafkaReqTopic;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import static koh.service.manager.vps.kafka.KafkaReqTopic.*;

public class App {
    final KafkaConfig kafkaConfig;
    final KafkaConsumer<String, String> consumer;
    final KafkaProducer<String, String> producer;
    final KafkaConsumerWorker consumerWorker;
    final KafkaProducerWorker producerWorker;

    static {
        DataHub.connect(
                AppConfig.MARIADB_HOST,
                AppConfig.MARIADB_PORT,
                AppConfig.MARIADB_PASSWORD,
                AppConfig.MARIADB_PASSWORD,
                AppConfig.MARIADB_DATABASE
        );
    }

    App() {
        this.kafkaConfig = new KafkaConfig(AppConfig.KAFKA_HOST, AppConfig.KAFKA_PORT, AppConfig.KAFKA_GROUP);
        this.consumer = new KafkaConsumer<>(this.kafkaConfig.getConsumerProperties());
        this.producer = new KafkaProducer<>(this.kafkaConfig.getProducerProperties());
        this.consumerWorker = new KafkaConsumerWorker(this.consumer);
        this.producerWorker = new KafkaProducerWorker(this.producer);
    }

    void start() {
        consumerWorker.addHandler(TOPIC_VPS_CONNECT_REQUEST, new ConnectDockerHandler(producerWorker));
        consumerWorker.addHandler(TOPIC_VPS_START_REQUEST, new StartDockerHandler(producerWorker));
        consumerWorker.addHandler(TOPIC_VPS_STOP_REQUEST, new StopDockerHandler(producerWorker));
        consumerWorker.addHandler(TOPIC_VPS_CREATE_CONTAINER_REQUEST, new CreateContainerHandler(producerWorker));
        consumerWorker.addHandler(TOPIC_VPS_CREATE_IMAGE_REQUEST, new CreateImageHandler(producerWorker));
        consumerWorker.addHandler(TOPIC_VPS_CREATE_NETWORK_REQUEST, new CreateNetworkHandler(producerWorker));
        consumerWorker.addHandler(TOPIC_VPS_CREATE_VOLUME_REQUEST, new CreateVolumeHandler(producerWorker));
        consumerWorker.addHandler(TOPIC_VPS_CREATE_VPS_REQUEST, new CreateVpsHandler(producerWorker));

        consumerWorker.exec();
    }

    public static void main(String[] args)
            throws Exception {
        new App().start();
    }
}
