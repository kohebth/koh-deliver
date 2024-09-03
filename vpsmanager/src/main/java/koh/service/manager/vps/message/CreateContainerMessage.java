package koh.service.manager.vps.message;

import lombok.Data;

@Data
public class CreateContainerMessage {
    Long userId;
    String name;
    Integer memory;
}
