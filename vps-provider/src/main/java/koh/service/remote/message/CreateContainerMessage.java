package koh.service.remote.message;

import koh.service.remote.kafka.Topic;
import lombok.Getter;

@Getter
public class CreateContainerMessage {
    Long userId;
    String name;

    public Topic getTopic() {
        return Topic.CREATE_CONTAINER;
    }
}
