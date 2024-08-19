package koh.service.remote.message;

import lombok.Getter;

@Getter
public class CreateImageMessage {
    String name;
    String repo;
    String version;
}
