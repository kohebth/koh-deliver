package koh.service.manager.vps.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateNetworkCmd;
import com.github.dockerjava.api.command.ListNetworksCmd;
import com.github.dockerjava.api.command.RemoveNetworkCmd;
import koh.db.hub.vps_management.tables.records.DockerNetworkRecord;

import java.util.List;

public interface NetworkRemote extends Connect {
    default String create(DockerNetworkRecord dockerNetwork) {
        com.github.dockerjava.api.model.Network.Ipam.Config ipamConfig = new com.github.dockerjava.api.model.Network.Ipam.Config()
                .withGateway(dockerNetwork.getGateway())
                .withSubnet(dockerNetwork.getSubnet())
                .withIpRange(dockerNetwork.getIpRange());
        com.github.dockerjava.api.model.Network.Ipam ipam = new com.github.dockerjava.api.model.Network.Ipam().withConfig(ipamConfig);
        try (CreateNetworkCmd cmd = command(DockerClient::createNetworkCmd)) {
            return cmd.withName(dockerNetwork.getName()).withDriver("bridge").withIpam(ipam).exec().getId();
        }
    }

    default void remove(DockerNetworkRecord dockerNetwork) {
        try (RemoveNetworkCmd cmd = command(DockerClient::removeNetworkCmd, dockerNetwork.getName())) {
            cmd.withNetworkId(dockerNetwork.getName()).exec();
        }
    }

    default String doesExist(DockerNetworkRecord network) {
        try (ListNetworksCmd cmd = command(DockerClient::listNetworksCmd)) {
            List<com.github.dockerjava.api.model.Network> networks = cmd.withNameFilter(network.getName()).exec();
            return networks.isEmpty() ? null : networks.get(0).getId();
        }
    }
}
