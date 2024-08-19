package koh.service.remote.message;

import lombok.Getter;

@Getter
public class CreateNetworkMessage {
    String name;
    String subnet;
    String gateway;
    String ipRange;
}
