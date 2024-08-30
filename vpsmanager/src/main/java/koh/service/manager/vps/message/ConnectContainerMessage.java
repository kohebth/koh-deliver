package koh.service.manager.vps.message;

import lombok.Getter;

import java.util.List;

@Getter
public class ConnectContainerMessage {
    Long containerId;
    Long imageId;
    List<Long> networkIds;
    List<Long> volumeIds;
}
