package koh.service.remote.service;

import koh.core.base.AbstractService;
import koh.core.base.HttpBody;
import koh.core.base.HttpParameter;
import koh.core.base.SimpleEnvelope;
import koh.core.base.impl.EmptyCookie;
import koh.core.base.impl.EmptyHeader;
import koh.core.base.impl.EmptyParameter;
import koh.core.helper.EnvelopeTools;
import koh.db.hub.repository.ImageRepository;
import koh.db.hub.vps_management.tables.records.DockerImageRecord;
import koh.service.remote.docker.DockerRemote;
import lombok.AllArgsConstructor;

public class CreateImageService extends AbstractService {
    final ImageRepository imageRepository = new ImageRepository();
    final DockerRemote dockerRemote = new DockerRemote();

    public CreateImageService() {
        super(EmptyParameter.class, EmptyHeader.class, EmptyCookie.class, Body.class);
    }

    @Override
    public SimpleEnvelope handle(HttpParameter parameter, SimpleEnvelope envelope)
            throws Exception {
        Body body = envelope.getBody();

        DockerImageRecord image = imageRepository.createImage(body.name, body.repo, body.version);
        if (image != null && image.getId() != null) {
            return EnvelopeTools.make(new ResponseBody(image.getId()),
                    EnvelopeTools.EMPTY_HEADER,
                    EnvelopeTools.EMPTY_COOKIE
            );
        }
        return EnvelopeTools.make(EnvelopeTools.EMPTY_BODY, EnvelopeTools.EMPTY_HEADER, EnvelopeTools.EMPTY_COOKIE);
    }


    public static class Body extends HttpBody {
        String name;
        String repo;
        String version;
    }

    @AllArgsConstructor
    public static class ResponseBody extends HttpBody {
        final Long dockerImageId;
    }
}
