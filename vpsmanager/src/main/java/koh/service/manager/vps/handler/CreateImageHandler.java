package koh.service.manager.vps.handler;

import koh.db.hub.repository.ImageRepository;
import koh.db.hub.vps_management.tables.records.DockerImageRecord;
import koh.service.manager.vps.kafka.KafkaProducerWorker;
import koh.service.manager.vps.message.CreateImageMessage;
import koh.service.manager.vps.tools.JsonTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Slf4j
public class CreateImageHandler extends AbstractRemoteHandler {
    ImageRepository networkRepository = new ImageRepository();

    public CreateImageHandler(KafkaProducerWorker bus) {
        super(bus);
    }

    @Override
    public boolean accept(ConsumerRecord<String, String> rawMessage)
            throws Exception {
        CreateImageMessage message = JsonTools.fromJson(rawMessage.value(), CreateImageMessage.class);
        DockerImageRecord image = networkRepository.createImage(
                message.getName(),
                message.getRepo(),
                message.getVersion()
        );
        return image != null && image.getId() != null;
    }
}
