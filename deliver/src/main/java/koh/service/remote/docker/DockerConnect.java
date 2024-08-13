package koh.service.remote.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.DockerCmd;
import koh.db.hub.vps_management.tables.records.DockerImageRecord;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface DockerConnect {
    <T, R extends DockerCmd<T>> R command(Function<DockerClient, R> cmd);

    <T, R extends DockerCmd<T>> R command(BiFunction<DockerClient, String, R> cmd, String s);

//    <R, T extends DockerCmd<R>> R command(Function<DockerClient, T> cmd);

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
