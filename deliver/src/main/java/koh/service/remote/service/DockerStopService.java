package koh.service.remote.service;

import koh.core.base.AbstractService;
import koh.core.base.HttpBody;
import koh.core.base.HttpParameter;
import koh.core.base.SimpleEnvelope;
import koh.core.base.impl.EmptyCookie;
import koh.core.base.impl.EmptyHeader;
import koh.core.base.impl.EmptyParameter;
import koh.core.helper.EnvelopeTools;
import koh.db.hub.repository.ContainerRepository;
import koh.db.hub.vps_management.tables.records.DockerContainerRecord;
import koh.service.remote.docker.DockerRemote;
import lombok.RequiredArgsConstructor;

public class DockerStopService extends AbstractService {
    final ContainerRepository containerRepository = new ContainerRepository();
    final DockerRemote docker = new DockerRemote();

    protected DockerStopService() {
        super(EmptyParameter.class, EmptyHeader.class, EmptyCookie.class, Body.class);
    }

    @Override
    public SimpleEnvelope handle(HttpParameter parameter, SimpleEnvelope envelope)
            throws Exception {
        Body body = envelope.getBody();

        DockerContainerRecord container = containerRepository.getContainerById(body.containerId);

        docker.stop(container);

        return EnvelopeTools.make(new ResponseBody("OK"), EnvelopeTools.EMPTY_HEADER, EnvelopeTools.EMPTY_COOKIE);
    }

    public static class Body extends HttpBody {
        Long userId;
        Long containerId;
    }

    @RequiredArgsConstructor
    public static class ResponseBody extends HttpBody {
        final String status;
    }
}
