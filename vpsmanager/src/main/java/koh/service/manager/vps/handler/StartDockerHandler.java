package koh.service.manager.vps.handler;

import koh.db.hub.metadata.MetaEnv;
import koh.db.hub.repository.*;
import koh.db.hub.vps_management.enums.UserMetadataType;
import koh.db.hub.vps_management.tables.records.*;
import koh.service.manager.vps.docker.Remote;
import koh.service.manager.vps.kafka.KafkaProducerWorker;
import koh.service.manager.vps.message.ContainerIdMessage;
import koh.service.manager.vps.message.StatusMessage;
import koh.service.manager.vps.tools.JsonTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;

import static koh.service.manager.vps.kafka.KafkaRespTopic.TOPIC_VPS_START_RESPONSE;

@Slf4j
public class StartDockerHandler extends AbstractRemoteHandler {
    ImageRepository imageRepository = new ImageRepository();
    VolumeRepository volumeRepository = new VolumeRepository();
    ContainerRepository containerRepository = new ContainerRepository();
    NetworkRepository networkRepository = new NetworkRepository();
    CompositeRepository compositeRepository = new CompositeRepository();
    UserMetadataRepository userMetadataRepository = new UserMetadataRepository();
    Remote docker = new Remote();

    public StartDockerHandler(KafkaProducerWorker bus) {
        super(bus);
    }

    @Override
    public void handle(ConsumerRecord<String, String> rawMessage)
            throws Exception {
        ContainerIdMessage message = JsonTools.fromJson(rawMessage.value(), ContainerIdMessage.class);

        CompositeRepository.ContainerSetup setup = compositeRepository.getContainerSetup(message.getContainerId());

        DockerImageRecord image = imageRepository.getImageById(setup.getImageId());
        DockerContainerRecord container = containerRepository.getContainerById(setup.getContainerId());
        List<DockerNetworkRecord> networks = networkRepository.getNetworkByIds(setup.getNetworkIds());
        List<DockerVolumeRecord> volumes = volumeRepository.getVolumeByIds(setup.getVolumeIds());
        UserMetadataRecord userMetadataRecord = userMetadataRepository.getMetadata(
                setup.getUserId(),
                UserMetadataType.ENVIRONMENT
        ).orElseThrow();

        MetaEnv metaEnv = JsonTools.fromJson(userMetadataRecord.getBlob());

        for (DockerNetworkRecord network : networks) {
            if (docker.doesExist(network) == null) {
                docker.create(network);
            }
        }

        for (DockerVolumeRecord volume : volumes) {
            if (docker.doesExist(volume) == null) {
                docker.create(volume);
            }
        }

        if (docker.doesExist(image) == null) {
            docker.createImage(image);
        }

        String dockerContainerId = docker.doesExist(container);
        if (dockerContainerId == null) {
            MetaEnv.Credential credential = metaEnv.getCredentials().get(container.getId());
            dockerContainerId = docker.create(container, image, networks, volumes, credential);
        }

        docker.start(container);

        if (dockerContainerId != null && docker.isRunning(container) != null) {
            bus.respond(TOPIC_VPS_START_RESPONSE, rawMessage.key(), new StatusMessage("Success"));
        } else {
            bus.respond(TOPIC_VPS_START_RESPONSE, rawMessage.key(), new StatusMessage("Failure"));
            throw new IllegalStateException(String.format("Container %s is unable to start", container));
        }
    }
}
