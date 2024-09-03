package koh.service.manager.vps.handler;

import koh.db.hub.repository.VolumeRepository;
import koh.db.hub.vps_management.tables.records.DockerVolumeRecord;
import koh.service.manager.vps.kafka.KafkaProducerWorker;
import koh.service.manager.vps.message.CreateVolumeMessage;
import koh.service.manager.vps.message.StatusMessage;
import koh.service.manager.vps.tools.JsonTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import static koh.service.manager.vps.kafka.KafkaRespTopic.TOPIC_VPS_CREATE_VOLUME_RESPONSE;

@Slf4j
public class CreateVolumeHandler extends AbstractRemoteHandler {
    VolumeRepository volumeRepository = new VolumeRepository();

    public CreateVolumeHandler(KafkaProducerWorker bus) {
        super(bus);
    }

    @Override
    public void handle(ConsumerRecord<String, String> rawMessage)
            throws Exception {
        CreateVolumeMessage message = JsonTools.fromJson(rawMessage.value(), CreateVolumeMessage.class);
        DockerVolumeRecord volume = volumeRepository.createVolume(message.getName(),
                message.getHost(),
                message.getVirtual(),
                message.getSizeInMb()
        );
        if (volume.insert() == 1) {
            bus.respond(TOPIC_VPS_CREATE_VOLUME_RESPONSE, rawMessage.key(), new StatusMessage(volume.toString()));
        } else {
            bus.respond(TOPIC_VPS_CREATE_VOLUME_RESPONSE, rawMessage.key(), new StatusMessage("Failure"));
        }
    }
}
