package utils.constants.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Constants implements Serializable  {

    @JsonProperty
    private String hubIp;

    @JsonProperty
    private String ownerName;

    @JsonProperty
    private int nodePort;

    @JsonProperty
    private int hubPort;

    public String getHubIp() {
        return hubIp;
    }

    public void setHubIp(final String hubIp) {
        this.hubIp = hubIp;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(final String ownerName) {
        this.ownerName = ownerName;
    }

    public int getNodePort() {
        return nodePort;
    }

    public void setNodePort(final int nodePort) {
        this.nodePort = nodePort;
    }

    public int getHubPort() {
        return hubPort;
    }

    public void setHubPort(final int hubPort) {
        this.hubPort = hubPort;
    }
}
