package koh.service.remote.handler;

import koh.db.hub.repository.ContainerRepository;
import koh.db.hub.vps_management.tables.records.DockerContainerRecord;
import koh.service.remote.docker.DockerRemote;
import koh.service.remote.kafka.Topic;
import koh.service.remote.message.ContainerIdMessage;
import koh.service.remote.tools.JsonTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

@Slf4j
public class StopDockerHandler extends AbstractRemoteHandler {
    final ContainerRepository containerRepository = new ContainerRepository();
    final DockerRemote docker = new DockerRemote();

    public StopDockerHandler(Producer<String, String> producer, Consumer<String, String> consumer) {
        super(producer, consumer);
    }

    @Override
    public boolean accept(ConsumerRecord<String, String> rawMessage)
            throws Exception {

        ContainerIdMessage message = JsonTools.fromJson(rawMessage.value(), ContainerIdMessage.class);

        DockerContainerRecord container = containerRepository.getContainerById(message.getContainerId());

        docker.stop(container);

        if (docker.isRunning(container) == null) {
            return true;
        }
        throw new IllegalStateException(String.format("Container %s is unable to stop", container));
    }

    @Override
    public ProducerRecord<String, String> produce()
            throws Exception {
        return null;
    }

    @Override
    protected String topic() {
        return Topic.STOP_DOCKER.name();
    }
}
