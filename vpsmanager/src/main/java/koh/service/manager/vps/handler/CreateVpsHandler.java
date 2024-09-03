package koh.service.manager.vps.handler;

import koh.db.hub.metadata.MetaEnv;
import koh.db.hub.repository.*;
import koh.db.hub.vps_management.enums.UserMetadataType;
import koh.db.hub.vps_management.tables.records.*;
import koh.service.manager.vps.kafka.KafkaProducerWorker;
import koh.service.manager.vps.message.ConnectContainerMessage;
import koh.service.manager.vps.message.CreateVpsMessage;
import koh.service.manager.vps.message.StatusMessage;
import koh.service.manager.vps.tools.JsonTools;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.IOException;
import java.util.List;

import static koh.service.manager.vps.kafka.KafkaReqTopic.TOPIC_VPS_CONNECT_REQUEST;
import static koh.service.manager.vps.kafka.KafkaRespTopic.TOPIC_VPS_CREATE_VPS_RESPONSE;

public class CreateVpsHandler extends AbstractRemoteHandler {
    public CreateVpsHandler(KafkaProducerWorker bus) {
        super(bus);
    }

    @Override
    public void handle(ConsumerRecord<String, String> rawMessage)
            throws Exception {
        CreateVpsMessage message = JsonTools.fromJson(rawMessage.value(), CreateVpsMessage.class);

        try {
            checkContainer(message.getVpsName());
            MetaEnv metaEnv = requireMetaEnv(message.getUserId());
            checkVolumeSize(metaEnv, message.getVolumeSizeInMb());
            checkMemorySize(metaEnv, message.getMemorySizeInMb());

            DockerImageRecord image = getImage(message.getImageId());
            DockerNetworkRecord network = getNetwork(message.getNetworkId());
            DockerContainerRecord container = newContainer(message.getUserId(),
                    message.getVpsName(),
                    message.getMemorySizeInMb()
            );
            DockerVolumeRecord volume = newVolume(message.getVpsName(), message.getVolumeSizeInMb());

            connectDockerComponent(
                    rawMessage.key(),
                    message.getUserId(),
                    container.getId(),
                    image.getId(),
                    network.getId(),
                    volume.getId()
            );

            bus.respond(TOPIC_VPS_CREATE_VPS_RESPONSE,
                    rawMessage.key(),
                    "We are setting up you VPS! An email with a credential will arrive to you soon!"
            );
        } catch (CreateVpsException e) {
            bus.respond(TOPIC_VPS_CREATE_VPS_RESPONSE, rawMessage.key(), new StatusMessage(e.getMessage()));
        }
    }

    void connectDockerComponent(
            String key, Long userId, Long containerId, Long imageId, Long networkId, Long volumeId
    )
            throws IOException {
        ConnectContainerMessage m = new ConnectContainerMessage();
        m.setUserId(userId);
        m.setContainerId(containerId);
        m.setImageId(imageId);
        m.setNetworkIds(List.of(networkId));
        m.setVolumeIds(List.of(volumeId));
        bus.request(TOPIC_VPS_CONNECT_REQUEST, key, JsonTools.toJson(m));
    }

    MetaEnv requireMetaEnv(Long userId)
            throws IOException {
        byte[] blob = new UserMetadataRepository().getMetadata(userId, UserMetadataType.ENVIRONMENT).map(
                UserMetadataRecord::getBlob).orElseThrow();
        return JsonTools.fromJson(blob, MetaEnv.class);
    }

    void checkContainer(String containerName) {
        if (new ContainerRepository().getContainerByName(containerName) != null) {
            throw new CreateVpsException("Container name is not available");
        }
    }

    void checkVolumeSize(MetaEnv metaEnv, Integer size) {
        if (metaEnv.getVolumeLimit() < size) {
            throw new CreateVpsException("Your volume is limited to " + metaEnv.getVolumeLimit() + "MB");
        }
    }

    void checkMemorySize(MetaEnv metaEnv, Integer size) {
        if (metaEnv.getMemoryLimit() < size) {
            throw new CreateVpsException("Your memory is limited to " + metaEnv.getVolumeLimit() + "MB");
        }
    }

    DockerContainerRecord newContainer(Long userId, String containerName, Integer sizeInMb) {
        DockerContainerRecord container = new ContainerRepository().createContainer(userId, containerName, sizeInMb);
        if (container == null) {
            throw new CreateVpsException("Create container error!");
        }
        return container;
    }

    DockerVolumeRecord newVolume(String volumeName, Integer sizeInMb) {
        DockerVolumeRecord volume = new VolumeRepository().createVolume(volumeName, "", "/home", sizeInMb);
        if (volume == null) {
            throw new CreateVpsException("Create volume error!");
        }
        return volume;
    }

    DockerNetworkRecord getNetwork(Long networkId) {
        DockerNetworkRecord network = new NetworkRepository().getNetworkById(networkId);
        if (network == null) {
            throw new CreateVpsException("Network is not found!");
        }
        return network;
    }

    DockerImageRecord getImage(Long imageId) {
        DockerImageRecord image = new ImageRepository().getImageById(imageId);
        if (image == null) {
            throw new CreateVpsException("Image is not found!");
        }
        return image;
    }

    static class CreateVpsException extends RuntimeException {
        CreateVpsException(String message) {
            super(message);
        }
    }
}
