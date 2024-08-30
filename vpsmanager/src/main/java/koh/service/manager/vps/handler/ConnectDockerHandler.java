package koh.service.manager.vps.handler;

import koh.db.hub.repository.*;
import koh.db.hub.vps_management.tables.records.DockerContainerRecord;
import koh.db.hub.vps_management.tables.records.DockerImageRecord;
import koh.db.hub.vps_management.tables.records.DockerNetworkRecord;
import koh.db.hub.vps_management.tables.records.DockerVolumeRecord;
import koh.service.manager.vps.docker.Remote;
import koh.service.manager.vps.kafka.KafkaProducerWorker;
import koh.service.manager.vps.kafka.KafkaRespTopic;
import koh.service.manager.vps.message.ConnectContainerMessage;
import koh.service.manager.vps.message.StatusMessage;
import koh.service.manager.vps.tools.JsonTools;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;

import static koh.service.manager.vps.kafka.KafkaRespTopic.TOPIC_VPS_CONNECT_RESPONSE;

public class ConnectDockerHandler extends AbstractRemoteHandler {
    final ContainerRepository containerRepository = new ContainerRepository();
    final NetworkRepository networkRepository = new NetworkRepository();
    final VolumeRepository volumeRepository = new VolumeRepository();
    final ImageRepository imageRepository = new ImageRepository();

    final ContainerNetworkRepository containerNetworkRepository = new ContainerNetworkRepository();
    final ContainerImageRepository containerImageRepository = new ContainerImageRepository();
    final ContainerVolumeRepository containerVolumeRepository = new ContainerVolumeRepository();

    public ConnectDockerHandler(KafkaProducerWorker bus) {
        super(bus);
    }

    @Override
    public boolean accept(ConsumerRecord<String, String> rawMessage)
            throws Exception {
        ConnectContainerMessage message = JsonTools.fromJson(rawMessage.value(), ConnectContainerMessage.class);

        DockerContainerRecord container = containerRepository.getContainerById(message.getContainerId());
        DockerImageRecord image = imageRepository.getImageById(message.getImageId());
        List<DockerNetworkRecord> networks = networkRepository.getNetworkByIds(message.getNetworkIds());
        List<DockerVolumeRecord> volumes = volumeRepository.getVolumeByIds(message.getVolumeIds());

        if (container != null && image != null && networks != null && !volumes.isEmpty()) {
            containerImageRepository.createContainerImageConnect(container, image);
            networks.forEach(network -> containerNetworkRepository.createContainerNetworkConnect(container, network));
            volumes.forEach(volume -> containerVolumeRepository.createContainerVolumeConnect(container, volume));

            Remote remote = new Remote();
            remote.start(container);
            bus.respond(TOPIC_VPS_CONNECT_RESPONSE, rawMessage.key(), new StatusMessage("Success"));
            return true;
        }
        bus.respond(TOPIC_VPS_CONNECT_RESPONSE, rawMessage.key(), new StatusMessage("Failure"));
        return false;
    }
}
