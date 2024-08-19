package koh.service.remote.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import koh.db.hub.vps_management.tables.records.DockerVolumeRecord;

import java.util.List;
import java.util.Map;

public interface DockerVolume extends DockerConnect {
    default String create(DockerVolumeRecord dockerVolume) {
        try (CreateVolumeCmd cmd = command(DockerClient::createVolumeCmd)) {
            return cmd
                    .withName(dockerVolume.getName())
                    .withDriverOpts(Map.of("device",
                            dockerVolume.getHost(),
                            "type",
                            dockerVolume.getType().name().toLowerCase()
                    ))
                    .exec()
                    .getName();
        }
    }

    default void remove(DockerVolumeRecord dockerVolume) {
        try (RemoveVolumeCmd cmd = command(DockerClient::removeVolumeCmd, dockerVolume.getName())) {
            cmd.withName(dockerVolume.getName()).exec();
        }
    }

    default String doesExist(DockerVolumeRecord volume) {
        try (ListVolumesCmd cmd = command(DockerClient::listVolumesCmd)) {
            ListVolumesResponse volumes = cmd.withFilter("name", List.of(volume.getName())).exec();
            List<InspectVolumeResponse> inspectVolumes = volumes.getVolumes();
            return inspectVolumes == null || inspectVolumes.isEmpty() ? null : inspectVolumes.get(0).getName();
        }
    }
}
