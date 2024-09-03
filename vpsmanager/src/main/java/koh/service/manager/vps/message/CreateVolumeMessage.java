package koh.service.manager.vps.message;

import lombok.Data;

@Data
public class CreateVolumeMessage {
    String name;
    String host;
    String virtual;
    Integer sizeInMb;
}
