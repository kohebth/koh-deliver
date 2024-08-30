package koh.service.manager.vps.message;

import lombok.Getter;

@Getter
public class ContainerIdMessage {
    Long containerId;

    enum Action {
        START, STOP;
    }
}
