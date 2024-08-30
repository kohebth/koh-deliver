package koh.service.manager.vps.message;

import lombok.Getter;

@Getter
public class CreateImageMessage {
    String name;
    String repo;
    String version;
}
