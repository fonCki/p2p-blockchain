package network.model.payload;

import network.model.MessageType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NodeInfoList extends Payload{
    private List<NodeInfo> nodeInfoList;
    public NodeInfoList() {
        super(Type.NODE_INFO_LIST);
        nodeInfoList = new ArrayList<>();
    }
    public NodeInfoList(List<NodeInfo> nodeInfoList) {
        super(Type.NODE_INFO_LIST);
        this.nodeInfoList = nodeInfoList;
    }
    public NodeInfoList(Map<String,NodeInfo> nodeInfoMap) {
        super(Type.NODE_INFO_LIST);
        this.nodeInfoList = new ArrayList<>(nodeInfoMap.values());
    }
    public NodeInfoList(Set<NodeInfo> nodeInfoSet) {
        super(Type.NODE_INFO_LIST);
        this.nodeInfoList = new ArrayList<>(nodeInfoSet);
    }

    public List<NodeInfo> getNodeInfoList() {
        return nodeInfoList;
    }
    public void setNodeInfoList(List<NodeInfo> nodeInfoList) {
        this.nodeInfoList = nodeInfoList;
    }
}
