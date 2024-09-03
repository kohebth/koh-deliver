package koh.service.manager.vps.message;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
public class ConnectContainerMessage {
    Long userId;
    Long containerId;
    Long imageId;
    List<Long> networkIds;
    List<Long> volumeIds;
}
