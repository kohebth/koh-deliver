package koh.service.remote.message;

import lombok.Getter;

import java.util.List;

@Getter
public class ConnectContainerMessage {
    Long containerId;
    Long imageId;
    Long networkId;
    List<Long> volumeIds;
}
