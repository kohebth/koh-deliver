package koh.service.remote.handler;

import koh.db.hub.repository.ContainerRepository;
import koh.db.hub.repository.NetworkRepository;
import koh.db.hub.vps_management.tables.records.DockerNetworkRecord;
import koh.service.remote.kafka.Topic;
import koh.service.remote.message.CreateNetworkMessage;
import koh.service.remote.tools.JsonTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

@Slf4j
public class CreateNetworkHandler extends AbstractRemoteHandler {
    ContainerRepository containerRepository = new ContainerRepository();
    NetworkRepository networkRepository = new NetworkRepository();

    public CreateNetworkHandler(Producer<String, String> producer, Consumer<String, String> consumer) {
        super(producer, consumer);
    }

    @Override
    public boolean accept(ConsumerRecord<String, String> rawMessage)
            throws Exception {


        CreateNetworkMessage message = JsonTools.fromJson(rawMessage.value(), CreateNetworkMessage.class);
        DockerNetworkRecord network = networkRepository.createNetwork(message.getName(),
                message.getSubnet(),
                message.getGateway(),
                message.getIpRange()
        );
        return network != null && network.getId() != null;
    }

    @Override
    public ProducerRecord<String, String> produce()
            throws Exception {
        return null;
    }

    @Override
    protected String topic() {
        return Topic.CREATE_NETWORK.name();
    }
}
