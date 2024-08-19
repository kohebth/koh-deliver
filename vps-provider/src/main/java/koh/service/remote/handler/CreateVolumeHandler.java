package koh.service.remote.handler;

import koh.db.hub.repository.VolumeRepository;
import koh.db.hub.vps_management.tables.records.DockerVolumeRecord;
import koh.service.remote.kafka.Topic;
import koh.service.remote.message.CreateVolumeMessage;
import koh.service.remote.tools.JsonTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

@Slf4j
public class CreateVolumeHandler extends AbstractRemoteHandler {
    VolumeRepository volumeRepository = new VolumeRepository();

    public CreateVolumeHandler(Producer<String, String> producer, Consumer<String, String> consumer) {
        super(producer, consumer);
    }

    @Override
    public boolean accept(ConsumerRecord<String, String> rawMessage)
            throws Exception {
        CreateVolumeMessage message = JsonTools.fromJson(rawMessage.value(), CreateVolumeMessage.class);
        DockerVolumeRecord volume = volumeRepository.createVolume(message.getName(),
                message.getHost(),
                message.getVirtual()
        );
        if (volume != null && volume.getId() != null) {
            return true;
        }
        return false;
    }

    @Override
    public ProducerRecord<String, String> produce()
            throws Exception {
        return null;
    }

    @Override
    protected String topic() {
        return Topic.CREATE_VOLUME.name();
    }
}
