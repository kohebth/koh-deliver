package koh.service.manager.vps.message;

import lombok.Getter;

@Getter
public class CreateVolumeMessage {
    String name;
    String host;
    String virtual;
    Integer sizeInMb;
}
