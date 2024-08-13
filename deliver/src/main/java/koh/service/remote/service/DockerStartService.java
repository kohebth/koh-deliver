package koh.service.remote.service;

import koh.core.base.AbstractService;
import koh.core.base.HttpBody;
import koh.core.base.HttpParameter;
import koh.core.base.SimpleEnvelope;
import koh.core.base.impl.EmptyCookie;
import koh.core.base.impl.EmptyHeader;
import koh.core.base.impl.EmptyParameter;
import koh.core.helper.EnvelopeTools;
import koh.db.hub.repository.*;
import koh.db.hub.vps_management.tables.records.DockerContainerRecord;
import koh.db.hub.vps_management.tables.records.DockerImageRecord;
import koh.db.hub.vps_management.tables.records.DockerNetworkRecord;
import koh.db.hub.vps_management.tables.records.DockerVolumeRecord;
import koh.service.remote.docker.DockerRemote;

import java.util.List;

public class DockerStartService extends AbstractService {

    final ImageRepository imageRepository = new ImageRepository();
    final VolumeRepository volumeRepository = new VolumeRepository();
    final NetworkRepository networkRepository = new NetworkRepository();
    final ContainerRepository containerRepository = new ContainerRepository();
    final CompositeRepository compositeRepository = new CompositeRepository();


    final DockerRemote docker = new DockerRemote();

    public DockerStartService() {
        super(EmptyParameter.class, EmptyHeader.class, EmptyCookie.class, Body.class);
    }

    @Override
    public SimpleEnvelope handle(HttpParameter parameter, SimpleEnvelope envelope)
            throws Exception {
        Body body = envelope.getBody();
        ResponseBody responseBody = new ResponseBody();

        CompositeRepository.ContainerSetup setup = compositeRepository.getContainerSetup(body.containerId);

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

        responseBody.setup = setup;
        responseBody.dockerContainerId = dockerContainerId;

        return EnvelopeTools.make(responseBody, EnvelopeTools.EMPTY_HEADER, EnvelopeTools.EMPTY_COOKIE);
    }

    public static class Body extends HttpBody {
        Long containerId;
    }

    public static class ResponseBody extends HttpBody {
        Object setup;
        String dockerContainerId;
    }
}
