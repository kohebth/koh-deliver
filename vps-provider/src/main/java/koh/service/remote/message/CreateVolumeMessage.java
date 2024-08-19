package koh.service.remote.message;

import lombok.Getter;

@Getter
public class CreateVolumeMessage {
    String name;
    String host;
    String virtual;
}
