package koh.service.manager.vps.handler;

import koh.db.hub.metadata.MetaEnv;
import koh.db.hub.repository.*;
import koh.db.hub.vps_management.enums.UserMetadataType;
import koh.db.hub.vps_management.tables.records.*;
import koh.service.manager.vps.kafka.KafkaProducerWorker;
import koh.service.manager.vps.message.ConnectContainerMessage;
import koh.service.manager.vps.message.StatusMessage;
import koh.service.manager.vps.tools.JsonTools;
import koh.service.manager.vps.tools.RandomTools;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static koh.service.manager.vps.kafka.KafkaReqTopic.TOPIC_MAIL_CREDENTIAL_REQUEST;
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
    public void handle(ConsumerRecord<String, String> rawMessage)
            throws Exception {
        ConnectContainerMessage message = JsonTools.fromJson(rawMessage.value(), ConnectContainerMessage.class);

        UserRecord user = new UserRepository().getUserById(message.getUserId()).orElse(null);
        DockerContainerRecord container = containerRepository.getContainerById(message.getContainerId());
        DockerImageRecord image = imageRepository.getImageById(message.getImageId());
        List<DockerNetworkRecord> networks = networkRepository.getNetworkByIds(message.getNetworkIds());
        List<DockerVolumeRecord> volumes = volumeRepository.getVolumeByIds(message.getVolumeIds());

        if (user != null && container != null && image != null && networks != null && !volumes.isEmpty()) {
            new UserContainerRepository().createUserContainerConnect(container, user);
            containerImageRepository.createContainerImageConnect(container, image);
            networks.forEach(network -> containerNetworkRepository.createContainerNetworkConnect(container, network));
            volumes.forEach(volume -> containerVolumeRepository.createContainerVolumeConnect(container, volume));

            UserMetadataRecord metadata = new UserMetadataRepository().getMetadata(user.getId(),
                    UserMetadataType.ENVIRONMENT
            ).orElseThrow();

            MetaEnv metaEnv = createCredential(metadata, container);
            new UserMetadataRepository().updateMetadata(metadata.getId(), JsonTools.toJsonBytes(metaEnv));

            bus.respond(TOPIC_VPS_CONNECT_RESPONSE, rawMessage.key(), new StatusMessage("Success"));

            bus.request(
                    TOPIC_MAIL_CREDENTIAL_REQUEST,
                    rawMessage.key(),
                    metaEnv.getCredentials().get(container.getId())
            );
        }
        bus.respond(TOPIC_VPS_CONNECT_RESPONSE, rawMessage.key(), new StatusMessage("Failure"));
    }

    MetaEnv createCredential(UserMetadataRecord metadata, DockerContainerRecord container)
            throws IOException {
        MetaEnv metaEnv = JsonTools.fromJson(metadata.getBlob(), MetaEnv.class);
        Map<Long, MetaEnv.Credential> credentials = metaEnv.getCredentials();
        credentials.computeIfAbsent(container.getId(), k -> {
            MetaEnv.Credential c = new MetaEnv.Credential();
            c.setType("vps-login");
            c.getContent().put("USER", container.getName());
            c.getContent().put("PASSWORD", RandomTools.randomPassword());
            return c;
        });
        return metaEnv;
    }
}
