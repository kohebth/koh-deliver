package koh.service.manager.vps.message;

import lombok.Getter;

@Getter
public class CreateContainerMessage {
    Long userId;
    String name;
}
