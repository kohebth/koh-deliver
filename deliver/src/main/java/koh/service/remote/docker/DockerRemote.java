package koh.service.remote.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.DockerCmd;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

import java.util.function.BiFunction;
import java.util.function.Function;

public class DockerRemote implements DockerConnect, DockerContainer, DockerNetwork, DockerImage, DockerVolume {
    final DockerClientConfig remoteConfig;

    public DockerRemote() {
        this.remoteConfig = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .build();
    }

    @Override
    public <T, R extends DockerCmd<T>> R command(Function<DockerClient, R> cmd) {
        return cmd.apply(DockerClientBuilder.getInstance(this.remoteConfig).build());
    }

    @Override
    public <T, R extends DockerCmd<T>> R command(BiFunction<DockerClient, String, R> cmd, String s) {
        return cmd.apply(DockerClientBuilder.getInstance(this.remoteConfig).build(), s);
    }
}
