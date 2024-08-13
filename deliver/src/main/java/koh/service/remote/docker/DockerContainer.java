package koh.service.remote.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import koh.db.hub.vps_management.tables.records.DockerContainerRecord;
import koh.db.hub.vps_management.tables.records.DockerImageRecord;
import koh.db.hub.vps_management.tables.records.DockerNetworkRecord;
import koh.db.hub.vps_management.tables.records.DockerVolumeRecord;

import java.util.List;
import java.util.stream.Collectors;

public interface DockerContainer extends DockerConnect {
    default void start(DockerContainerRecord container) {
        String containerName = container.getName();
        try (StartContainerCmd cmd = command(DockerClient::startContainerCmd, containerName)) {
            cmd.withContainerId(containerName).exec();
        }
    }

    default void stop(DockerContainerRecord container) {
        String containerName = container.getName();
        try (StopContainerCmd cmd = command(DockerClient::stopContainerCmd, containerName)) {
            cmd.withContainerId(containerName).exec();
        }
    }

    default String create(
            DockerContainerRecord dockerContainer,
            DockerImageRecord dockerImage,
            List<DockerNetworkRecord> dockerNetworks,
            List<DockerVolumeRecord> dockerVolumes
            //, List<UserMetadataRecord> userMetadatas
    ) {
        String dockerContainerId;

        List<Bind> binds = dockerVolumes
                .stream()
                .map(v -> new Bind(v.getName(), new Volume(v.getVirtual())))
                .collect(Collectors.toList());

        HostConfig hostConfig = HostConfig.newHostConfig().withBinds(binds);
        List<ExposedPort> exposedPorts = List.of(new ExposedPort(22));

        try (CreateContainerCmd cmd = command(c -> c.createContainerCmd(dockerContainer.getName()))) {
            dockerContainerId = cmd
                    .withName(dockerContainer.getName())
                    .withHostConfig(hostConfig)
                    .withExposedPorts(exposedPorts)
                    .withImage(dockerImage.getName() + ':' + dockerImage.getVersion())
                    .withEnv()
                    .exec()
                    .getId();
        }

        if (dockerContainerId != null) {
            try (ConnectToNetworkCmd cmd = command(DockerClient::connectToNetworkCmd)) {
                dockerNetworks.forEach(nw -> cmd
                        .withNetworkId(nw.getName())
                        .withContainerId(dockerContainer.getName())
                        .exec());
            }
        }

        return dockerContainerId;
    }

    default void remove(DockerContainerRecord dockerContainer) {
        try (RemoveContainerCmd cmd = command(DockerClient::removeContainerCmd, dockerContainer.getName())) {
            cmd.withContainerId(dockerContainer.getName()).exec();
        }
    }

    default String doesExist(DockerContainerRecord container) {
        try (ListContainersCmd cmd = command(DockerClient::listContainersCmd)) {
            List<Container> containers = cmd.withShowAll(true).withNameFilter(List.of(container.getName())).exec();
            return containers.isEmpty() ? null : containers.get(0).getId();
        }
    }
}
