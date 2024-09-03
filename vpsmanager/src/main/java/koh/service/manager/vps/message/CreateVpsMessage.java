package koh.service.manager.vps.message;

import lombok.Getter;

@Getter
public class CreateVpsMessage {
    Long userId;
    Long imageId;
    Long networkId;
    String vpsName;
    Integer volumeSizeInMb;
    Integer memorySizeInMb;
}
