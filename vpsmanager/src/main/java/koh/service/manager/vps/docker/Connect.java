package koh.service.manager.vps.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.DockerCmd;
import koh.db.hub.vps_management.tables.records.DockerImageRecord;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Connect {
    <T, R extends DockerCmd<T>> R command(Function<DockerClient, R> cmd);

//    <R, T extends DockerCmd<R>> R command(Function<DockerClient, T> cmd);

    <T, R extends DockerCmd<T>> R command(BiFunction<DockerClient, String, R> cmd, String s);
}
