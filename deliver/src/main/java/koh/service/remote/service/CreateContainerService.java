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
import lombok.AllArgsConstructor;

public class CreateContainerService extends AbstractService {
    final ContainerRepository containerRepository = new ContainerRepository();

    public CreateContainerService() {
        super(EmptyParameter.class, EmptyHeader.class, EmptyCookie.class, Body.class);
    }

    @Override
    public SimpleEnvelope handle(HttpParameter parameter, SimpleEnvelope envelope)
            throws Exception {
        Body body = envelope.getBody();
        DockerContainerRecord container = containerRepository.createContainer(body.userId, body.name);

        if (container != null && container.getId() != null) {
            return EnvelopeTools.make(new ResponseBody(container.getId()),
                    EnvelopeTools.EMPTY_HEADER,
                    EnvelopeTools.EMPTY_COOKIE
            );
        }
        return EnvelopeTools.make(EnvelopeTools.EMPTY_BODY, EnvelopeTools.EMPTY_HEADER, EnvelopeTools.EMPTY_COOKIE);
    }

    public static class Body extends HttpBody {
        Long userId;
        String name;
    }

    @AllArgsConstructor
    public static class ResponseBody extends HttpBody {
        final Long dockerContainerId;
    }
}
