package koh.service.manager.vps.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import koh.db.hub.vps_management.tables.records.DockerImageRecord;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface ImageRemote extends Connect {
    default void createImage(DockerImageRecord image)
            throws InterruptedException {
        String tag = imageTag(image);
        try (PullImageCmd cmd = command(DockerClient::pullImageCmd, tag)) {
            cmd.withTag(tag).exec(new PullImageResultCallback()).awaitCompletion(30, TimeUnit.SECONDS);
        }
    }

    default String doesExist(DockerImageRecord image) {
        try (ListImagesCmd cmd = command(DockerClient::listImagesCmd)) {
            List<com.github.dockerjava.api.model.Image> images = cmd.withReferenceFilter(imageTag(image)).exec();
            return images.isEmpty() ? null : images.get(0).getId();
        }
    }

    static String imageTag(DockerImageRecord image) {
        StringBuilder imageTagBuilder = new StringBuilder();
        if (!image.getRepo().isEmpty()) {
            imageTagBuilder.append(image.getRepo());
            imageTagBuilder.append("/");
        }
        imageTagBuilder.append(image.getName());
        if (!image.getVersion().isEmpty()) {
            imageTagBuilder.append(":");
            imageTagBuilder.append(image.getVersion());
        }
        return imageTagBuilder.toString();
    }
}
