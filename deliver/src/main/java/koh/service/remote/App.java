package koh.service.remote;

import koh.core.server.SimpleServer;
import koh.service.remote.service.CreateContainerService;
import koh.service.remote.service.CreateImageService;
import koh.service.remote.service.CreateNetworkService;
import koh.service.remote.service.CreateVolumeService;
import koh.service.remote.service.DockerStartService;
import lombok.extern.slf4j.Slf4j;

import static koh.core.base.HttpMethod.GET;
import static koh.core.base.HttpMethod.POST;

@Slf4j
public class App extends SimpleServer {
    public static void main(String[] args) {
        new App().start();
    }

    @Override
    protected void config() {
        host("0.0.0.0");
        port(8080);

        route(POST, "/container", CreateContainerService.class);
        route(POST, "/image", CreateImageService.class);
        route(POST, "/network", CreateNetworkService.class);
        route(POST, "/volume", CreateVolumeService.class);
        route(GET, "/start", DockerStartService.class);
    }
}
