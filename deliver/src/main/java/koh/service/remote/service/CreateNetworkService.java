package koh.service.remote.service;

import koh.core.base.AbstractService;
import koh.core.base.HttpBody;
import koh.core.base.HttpParameter;
import koh.core.base.SimpleEnvelope;
import koh.core.base.impl.EmptyCookie;
import koh.core.base.impl.EmptyHeader;
import koh.core.base.impl.EmptyParameter;
import koh.core.helper.EnvelopeTools;
import koh.db.hub.repository.NetworkRepository;
import koh.db.hub.vps_management.tables.records.DockerNetworkRecord;
import koh.service.remote.docker.DockerRemote;
import lombok.AllArgsConstructor;

public class CreateNetworkService extends AbstractService {
    final NetworkRepository networkRepository = new NetworkRepository();
    final DockerRemote dockerRemote = new DockerRemote();

    public CreateNetworkService() {
        super(EmptyParameter.class, EmptyHeader.class, EmptyCookie.class, Body.class);
    }

    @Override
    public SimpleEnvelope handle(HttpParameter parameter, SimpleEnvelope envelope)
            throws Exception {
        Body body = envelope.getBody();
        DockerNetworkRecord network = networkRepository.createNetwork(body.name,
                body.subnet,
                body.gateway,
                body.ipRange
        );
        if (network != null && network.getId() != null) {
            return EnvelopeTools.make(new ResponseBody(network.getId()),
                    EnvelopeTools.EMPTY_HEADER,
                    EnvelopeTools.EMPTY_COOKIE
            );
        }
        return EnvelopeTools.make(EnvelopeTools.EMPTY_BODY, EnvelopeTools.EMPTY_HEADER, EnvelopeTools.EMPTY_COOKIE);
    }

    public static class Body extends HttpBody {
        String name;
        String subnet;
        String gateway;
        String ipRange;
    }

    @AllArgsConstructor
    public static class ResponseBody extends HttpBody {
        final Long dockerNetworkId;
    }
}
