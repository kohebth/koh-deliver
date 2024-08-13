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
import koh.db.hub.vps_management.tables.records.*;
import koh.service.remote.docker.DockerRemote;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

public class ConnectContainerService extends AbstractService {
    final ContainerRepository containerRepository = new ContainerRepository();
    final NetworkRepository networkRepository = new NetworkRepository();
    final VolumeRepository volumeRepository = new VolumeRepository();
    final ImageRepository imageRepository = new ImageRepository();

    final ContainerNetworkRepository containerNetworkRepository = new ContainerNetworkRepository();
    final ContainerImageRepository containerImageRepository = new ContainerImageRepository();
    final ContainerVolumeRepository containerVolumeRepository = new ContainerVolumeRepository();

    final DockerRemote dockerRemote = new DockerRemote();

    public ConnectContainerService() {
        super(EmptyParameter.class, EmptyHeader.class, EmptyCookie.class, Body.class);
    }

    @Override
    public SimpleEnvelope handle(HttpParameter parameter, SimpleEnvelope envelope)
            throws Exception {
        Body body = envelope.getBody();

        DockerContainerRecord container = containerRepository.getContainerById(body.containerId);
        DockerImageRecord image = imageRepository.getImageById(body.imageId);
        DockerNetworkRecord network = networkRepository.getNetworkById(body.networkId);
        List<DockerVolumeRecord> volumes = volumeRepository.getVolumeByIds(body.volumeIds);

        if (container != null && image != null && network != null && !volumes.isEmpty()) {
            ContainerImageRecord cir = containerImageRepository.createContainerImageConnect(container, image);
            ContainerNetworkRecord cnr = containerNetworkRepository.createContainerNetworkConnect(container, network);
            List<ContainerVolumeRecord> cvrs = volumes
                    .stream()
                    .map(volume -> containerVolumeRepository.createContainerVolumeConnect(container, volume))
                    .collect(Collectors.toList());

            return EnvelopeTools.make(new ResponseBody(cir, cnr, cvrs),
                    EnvelopeTools.EMPTY_HEADER,
                    EnvelopeTools.EMPTY_COOKIE
            );
        }
        return EnvelopeTools.make(EnvelopeTools.EMPTY_BODY, EnvelopeTools.EMPTY_HEADER, EnvelopeTools.EMPTY_COOKIE);
    }

    public static class Body extends HttpBody {
        Long containerId;
        Long imageId;
        Long networkId;
        List<Long> volumeIds;
    }

    @AllArgsConstructor
    public static class ResponseBody extends HttpBody {
        final ContainerImageRecord containerImageRecord;
        final ContainerNetworkRecord containerNetworkRecord;
        final List<ContainerVolumeRecord> containerVolumeRecords;
    }
}
