package koh.service.remote.service;

import koh.core.base.AbstractService;
import koh.core.base.HttpBody;
import koh.core.base.HttpParameter;
import koh.core.base.SimpleEnvelope;
import koh.core.base.impl.EmptyCookie;
import koh.core.base.impl.EmptyHeader;
import koh.core.base.impl.EmptyParameter;
import koh.core.helper.EnvelopeTools;
import koh.db.hub.repository.VolumeRepository;
import koh.db.hub.vps_management.tables.records.DockerVolumeRecord;
import koh.service.remote.docker.DockerRemote;
import lombok.AllArgsConstructor;

public class CreateVolumeService extends AbstractService {
    final VolumeRepository volumeRepository = new VolumeRepository();
    final DockerRemote dockerRemote = new DockerRemote();

    public CreateVolumeService() {
        super(EmptyParameter.class, EmptyHeader.class, EmptyCookie.class, Body.class);
    }

    @Override
    public SimpleEnvelope handle(HttpParameter parameter, SimpleEnvelope envelope)
            throws Exception {
        Body body = envelope.getBody();
        DockerVolumeRecord volume = volumeRepository.createVolume(body.name, body.host, body.virtual);
        if (volume != null && volume.getId() != null) {
            return EnvelopeTools.make(new ResponseBody(volume.getId()),
                    EnvelopeTools.EMPTY_HEADER,
                    EnvelopeTools.EMPTY_COOKIE
            );
        }
        return EnvelopeTools.make(EnvelopeTools.EMPTY_BODY, EnvelopeTools.EMPTY_HEADER, EnvelopeTools.EMPTY_COOKIE);
    }

    public static class Body extends HttpBody {
        String name;
        String host;
        String virtual;
    }

    @AllArgsConstructor
    public static class ResponseBody extends HttpBody {
        final Long dockerVolumeId;
    }
}
