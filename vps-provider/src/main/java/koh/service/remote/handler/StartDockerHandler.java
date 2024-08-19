package koh.service.remote.handler;

import koh.db.hub.repository.*;
import koh.db.hub.vps_management.tables.records.DockerContainerRecord;
import koh.db.hub.vps_management.tables.records.DockerImageRecord;
import koh.db.hub.vps_management.tables.records.DockerNetworkRecord;
import koh.db.hub.vps_management.tables.records.DockerVolumeRecord;
import koh.service.remote.docker.DockerRemote;
import koh.service.remote.kafka.Topic;
import koh.service.remote.message.ContainerIdMessage;
import koh.service.remote.tools.JsonTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.List;

@Slf4j
public class StartDockerHandler extends AbstractRemoteHandler {
    ImageRepository imageRepository = new ImageRepository();
    VolumeRepository volumeRepository = new VolumeRepository();
    ContainerRepository containerRepository = new ContainerRepository();
    NetworkRepository networkRepository = new NetworkRepository();
    CompositeRepository compositeRepository = new CompositeRepository();
    DockerRemote docker = new DockerRemote();

    public StartDockerHandler(Producer<String, String> producer, Consumer<String, String> consumer) {
        super(producer, consumer);
    }

    @Override
    public boolean accept(ConsumerRecord<String, String> rawMessage)
            throws Exception {
        ContainerIdMessage message = JsonTools.fromJson(rawMessage.value(), ContainerIdMessage.class);

        CompositeRepository.ContainerSetup setup = compositeRepository.getContainerSetup(message.getContainerId());

        DockerImageRecord image = imageRepository.getImageById(setup.getImageId());
        DockerContainerRecord container = containerRepository.getContainerById(setup.getContainerId());
        List<DockerNetworkRecord> networks = networkRepository.getNetworkByIds(setup.getNetworkIds());
        List<DockerVolumeRecord> volumes = volumeRepository.getVolumeByIds(setup.getVolumeIds());

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
            dockerContainerId = docker.create(container, image, networks, volumes);
        }

        docker.start(container);

        if (dockerContainerId != null && docker.isRunning(container) != null) {
            return true;
        }
        throw new IllegalStateException(String.format("Container %s is unable to start", container));
    }

    @Override
    public ProducerRecord<String, String> produce()
            throws Exception {
        return null;
    }

    @Override
    protected String topic() {
        return Topic.START_DOCKER.name();
    }
}
