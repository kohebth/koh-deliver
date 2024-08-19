package koh.service.remote.handler;

import koh.db.hub.repository.ContainerRepository;
import koh.db.hub.vps_management.tables.records.DockerContainerRecord;
import koh.service.remote.kafka.Topic;
import koh.service.remote.message.CreateContainerMessage;
import koh.service.remote.tools.JsonTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

@Slf4j
public class CreateContainerHandler extends AbstractRemoteHandler {
    ContainerRepository containerRepository = new ContainerRepository();

    public CreateContainerHandler(Producer<String, String> producer, Consumer<String, String> consumer) {
        super(producer, consumer);
    }

    @Override
    public boolean accept(ConsumerRecord<String, String> rawMessage)
            throws Exception {

        CreateContainerMessage message = JsonTools.fromJson(rawMessage.value());

        DockerContainerRecord container = containerRepository.createContainer(message.getUserId(), message.getName());

        return container != null && container.getId() != null;
    }

    @Override
    public ProducerRecord<String, String> produce()
            throws Exception {
        return null;
    }

    @Override
    protected String topic() {
        return Topic.CREATE_CONTAINER.name();
    }
}
