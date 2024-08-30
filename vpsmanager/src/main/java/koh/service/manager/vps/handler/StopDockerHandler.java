package koh.service.manager.vps.handler;

import koh.db.hub.repository.ContainerRepository;
import koh.db.hub.vps_management.tables.records.DockerContainerRecord;
import koh.service.manager.vps.docker.Remote;
import koh.service.manager.vps.kafka.KafkaProducerWorker;
import koh.service.manager.vps.message.ContainerIdMessage;
import koh.service.manager.vps.message.StatusMessage;
import koh.service.manager.vps.tools.JsonTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import static koh.service.manager.vps.kafka.KafkaRespTopic.*;

@Slf4j
public class StopDockerHandler extends AbstractRemoteHandler {
    final ContainerRepository containerRepository = new ContainerRepository();
    final Remote docker = new Remote();

    public StopDockerHandler(KafkaProducerWorker bus) {
        super(bus);
    }


    @Override
    public boolean accept(ConsumerRecord<String, String> rawMessage)
            throws Exception {

        ContainerIdMessage message = JsonTools.fromJson(rawMessage.value(), ContainerIdMessage.class);

        DockerContainerRecord container = containerRepository.getContainerById(message.getContainerId());

        docker.stop(container);

        if (docker.isRunning(container) == null) {
            bus.respond(TOPIC_VPS_START_RESPONSE, rawMessage.key(), new StatusMessage("Success"));
            return true;
        }
        bus.respond(TOPIC_VPS_START_RESPONSE, rawMessage.key(), new StatusMessage("Failure"));
        throw new IllegalStateException(String.format("Container %s is unable to stop", container));
    }
}
