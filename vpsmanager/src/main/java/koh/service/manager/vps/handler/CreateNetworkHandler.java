package koh.service.manager.vps.handler;

import koh.db.hub.repository.ContainerRepository;
import koh.db.hub.repository.NetworkRepository;
import koh.db.hub.vps_management.tables.records.DockerNetworkRecord;
import koh.service.manager.vps.kafka.KafkaProducerWorker;
import koh.service.manager.vps.message.CreateNetworkMessage;
import koh.service.manager.vps.message.StatusMessage;
import koh.service.manager.vps.tools.JsonTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import static koh.service.manager.vps.kafka.KafkaRespTopic.TOPIC_VPS_CREATE_NETWORK_RESPONSE;

@Slf4j
public class CreateNetworkHandler extends AbstractRemoteHandler {
    ContainerRepository containerRepository = new ContainerRepository();
    NetworkRepository networkRepository = new NetworkRepository();

    public CreateNetworkHandler(KafkaProducerWorker bus) {
        super(bus);
    }

    @Override
    public void handle(ConsumerRecord<String, String> rawMessage)
            throws Exception {


        CreateNetworkMessage message = JsonTools.fromJson(rawMessage.value(), CreateNetworkMessage.class);
        DockerNetworkRecord network = networkRepository.createNetwork(message.getName(),
                message.getSubnet(),
                message.getGateway(),
                message.getIpRange()
        );
        if (network.insert() == 1) {
            bus.respond(TOPIC_VPS_CREATE_NETWORK_RESPONSE, rawMessage.key(), new StatusMessage(network.toString()));
        } else {
            bus.respond(TOPIC_VPS_CREATE_NETWORK_RESPONSE, rawMessage.key(), new StatusMessage("Failure"));
        }
    }
}
