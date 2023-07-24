package network.model.payload;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class NodeInfo extends Payload{

    public enum Status {
        LONELY,
        CONNECTED,
        DISCONNECTED
    }

    //Popularity is a number betwen -MAX_NODE_CONNECTIONS and MAX_NODE_CONNECTIONS (usually -8 and +8)
    //-8 means that I am connected to 8 nodes, but no one is connected to me
    //+8 means that 8 nodes are connected to me, but I am not connected to anyone
    //0 means that I am connected to 4 nodes and 4 nodes are connected to me (the ideal situation)
    private int popularity;
    private String nodeId;
    private String ipAddress;
    private int port;
    private String timeStamp;
    private Status status;

    public NodeInfo(String ipAddress, int port) {
        super(Payload.Type.NODE_INFO);
        this.nodeId = UUID.randomUUID().toString();
        this.ipAddress = ipAddress;
        this.port = port;
        this.timeStamp = Date.from(java.time.ZonedDateTime.now().toInstant()).toString();
        this.status = Status.LONELY;
        this.popularity = 0;
    }

    public NodeInfo(String nodeId, String ipAddress, int port) {
        super(Payload.Type.NODE_INFO);
        this.nodeId = nodeId;
        this.ipAddress = ipAddress;
        this.port = port;
        this.timeStamp = Date.from(java.time.ZonedDateTime.now().toInstant()).toString();
        this.status = Status.LONELY;
        this.popularity = 0;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeInfo nodeInfo)) return false;
        return port == nodeInfo.port && Objects.equals(nodeId, nodeInfo.nodeId) && Objects.equals(ipAddress, nodeInfo.ipAddress) && Objects.equals(timeStamp, nodeInfo.timeStamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, ipAddress, port, timeStamp);
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "nodeId='" + nodeId + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", port=" + port +
                ", timeStamp='" + timeStamp + '\'' +
                '}';
    }
}


