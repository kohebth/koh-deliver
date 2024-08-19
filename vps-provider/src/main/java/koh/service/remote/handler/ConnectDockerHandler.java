package koh.service.remote.handler;

import koh.db.hub.repository.*;
import koh.db.hub.vps_management.tables.records.DockerContainerRecord;
import koh.db.hub.vps_management.tables.records.DockerImageRecord;
import koh.db.hub.vps_management.tables.records.DockerNetworkRecord;
import koh.db.hub.vps_management.tables.records.DockerVolumeRecord;
import koh.service.remote.docker.DockerRemote;
import koh.service.remote.kafka.Topic;
import koh.service.remote.message.ConnectContainerMessage;
import koh.service.remote.tools.JsonTools;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.List;

public class ConnectDockerHandler extends AbstractRemoteHandler {
    final ContainerRepository containerRepository = new ContainerRepository();
    final NetworkRepository networkRepository = new NetworkRepository();
    final VolumeRepository volumeRepository = new VolumeRepository();
    final ImageRepository imageRepository = new ImageRepository();

    final ContainerNetworkRepository containerNetworkRepository = new ContainerNetworkRepository();
    final ContainerImageRepository containerImageRepository = new ContainerImageRepository();
    final ContainerVolumeRepository containerVolumeRepository = new ContainerVolumeRepository();

    final DockerRemote dockerRemote = new DockerRemote();

    public ConnectDockerHandler(
            Producer<String, String> producer,
            Consumer<String, String> consumer
    ) {
        super(producer, consumer);
    }

    @Override
    public boolean accept(ConsumerRecord<String, String> rawMessage)
            throws Exception {
        ConnectContainerMessage message = JsonTools.fromJson(rawMessage.value(), ConnectContainerMessage.class);

        DockerContainerRecord container = containerRepository.getContainerById(message.getContainerId());
        DockerImageRecord image = imageRepository.getImageById(message.getImageId());
        DockerNetworkRecord network = networkRepository.getNetworkById(message.getNetworkId());
        List<DockerVolumeRecord> volumes = volumeRepository.getVolumeByIds(message.getVolumeIds());

        if (container != null && image != null && network != null && !volumes.isEmpty()) {
            containerImageRepository.createContainerImageConnect(container, image);
            containerNetworkRepository.createContainerNetworkConnect(container, network);
            volumes.forEach(volume -> containerVolumeRepository.createContainerVolumeConnect(container, volume));

            DockerRemote remote = new DockerRemote();
            remote.start(container);
        }
        return true;
    }

    @Override
    public ProducerRecord<String, String> produce()
            throws Exception {
        return null;
    }

    @Override
    protected String topic() {
        return Topic.CONNECT_DOCKER.name();
    }
}
