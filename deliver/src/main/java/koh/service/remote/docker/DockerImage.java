package koh.service.remote.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.Image;
import koh.db.hub.vps_management.tables.records.DockerImageRecord;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface DockerImage extends DockerConnect {
    default void createImage(DockerImageRecord image)
            throws InterruptedException {
        String tag = DockerConnect.imageTag(image);
        try (PullImageCmd cmd = command(DockerClient::pullImageCmd, tag)) {
            cmd.withTag(tag).exec(new PullImageResultCallback()).awaitCompletion(30, TimeUnit.SECONDS);
        }
    }

    default String doesExist(DockerImageRecord image) {
        try (ListImagesCmd cmd = command(DockerClient::listImagesCmd)) {
            List<Image> images = cmd.withReferenceFilter(DockerConnect.imageTag(image)).exec();
            return images.isEmpty() ? null : images.get(0).getId();
        }
    }
}
