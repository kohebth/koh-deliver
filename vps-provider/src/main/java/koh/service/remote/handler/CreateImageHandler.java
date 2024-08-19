package koh.service.remote.handler;

import koh.db.hub.repository.ImageRepository;
import koh.db.hub.vps_management.tables.records.DockerImageRecord;
import koh.service.remote.kafka.Topic;
import koh.service.remote.message.CreateImageMessage;
import koh.service.remote.tools.JsonTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

@Slf4j
public class CreateImageHandler extends AbstractRemoteHandler {
    ImageRepository networkRepository = new ImageRepository();

    public CreateImageHandler(Producer<String, String> producer, Consumer<String, String> consumer) {
        super(producer, consumer);
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

    @Override
    public ProducerRecord<String, String> produce()
            throws Exception {
        return null;
    }

    @Override
    protected String topic() {
        return Topic.CREATE_IMAGE.name();
    }
}
