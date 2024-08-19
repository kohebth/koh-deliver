package koh.service.remote;

import koh.service.remote.handler.*;
import koh.service.remote.kafka.KafkaConfig;
import koh.service.remote.kafka.KafkaExecutor;
import koh.service.remote.kafka.Topic;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

public class App {
    final KafkaConfig kafkaConfig;
    final KafkaConsumer<String, String> consumer;
    final KafkaProducer<String, String> producer;
    final KafkaExecutor kafkaExecutor;

    App() throws Exception {
        this.kafkaConfig = new KafkaConfig(AppConfig.KAFKA_HOST, AppConfig.KAFKA_PORT, AppConfig.KAFKA_GROUP);
        this.consumer = new KafkaConsumer<>(this.kafkaConfig.getConsumerProperties());
        this.producer = new KafkaProducer<>(this.kafkaConfig.getProducerProperties());
        this.kafkaExecutor = new KafkaExecutor(this.consumer, this.producer);
    }

    void start() {
        this.kafkaExecutor.addHandler(Topic.CREATE_IMAGE, new CreateImageHandler(this.producer, this.consumer));
        this.kafkaExecutor.addHandler(Topic.CREATE_VOLUME, new CreateVolumeHandler(this.producer, this.consumer));
        this.kafkaExecutor.addHandler(Topic.CREATE_NETWORK, new CreateNetworkHandler(this.producer, this.consumer));
        this.kafkaExecutor.addHandler(Topic.CREATE_CONTAINER, new CreateContainerHandler(this.producer, this.consumer));
        this.kafkaExecutor.addHandler(Topic.CONNECT_DOCKER, new ConnectDockerHandler(this.producer, this.consumer));
        this.kafkaExecutor.addHandler(Topic.START_DOCKER, new StartDockerHandler(this.producer, this.consumer));
        this.kafkaExecutor.addHandler(Topic.STOP_DOCKER, new StopDockerHandler(this.producer, this.consumer));

        this.kafkaExecutor.exec();
    }

    public static void main(String[] args) throws Exception {
        new App().start();
    }
}
