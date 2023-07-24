package state;

import network.Connection;
import network.model.Message;
import network.model.payload.NodeInfo;
import network.model.payload.NodeInfoList;
import utils.ObservableConcurrentHashMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AppState {
    private static AppState instance = null;
    private NodeInfo myNodeInfo;
    private ConcurrentHashMap<String, NodeInfo> nodeInfoMap;
    private ObservableConcurrentHashMap<String, Connection> nodesIamConnectedTo;
    private ObservableConcurrentHashMap<String, NodeInfo> nodesConnectedToMe;
    private ConcurrentHashMap<String, Message> messageHashMap;
    public final int MAX_NODE_CONNECTIONS = 8;

    private AppState() {
        myNodeInfo = null;
        nodeInfoMap = new ConcurrentHashMap<>();
        Runnable callback = this::setPupularity;
        nodesIamConnectedTo = new ObservableConcurrentHashMap<>(callback);
        nodesConnectedToMe = new ObservableConcurrentHashMap<>(callback);
        messageHashMap = new ConcurrentHashMap<>();
    }

    public void clear() {
        myNodeInfo = null;
        nodeInfoMap.clear();
        nodesIamConnectedTo.clear();
        nodesConnectedToMe.clear();
    }

    public static synchronized AppState getInstance() {
        if (instance == null) {
            instance = new AppState();
        }
        return instance;
    }

    public void setMyNodeInfo(NodeInfo myNodeInfo) {
        this.myNodeInfo = myNodeInfo;
    }

    public void setStatusNodeInfo(NodeInfo.Status status) {
        this.myNodeInfo.setStatus(status);
    }

    public NodeInfo getMyNodeInfo() {
        return myNodeInfo;
    }

    // NodeInfoMap

    public void addNodeInfoMap(List<NodeInfo> nodeInfoList) {
        for (NodeInfo nodeInfo : nodeInfoList) {
            this.nodeInfoMap.put(nodeInfo.getNodeId(), nodeInfo);
        }
    }

    public void addNodeInfoMap(Set<NodeInfo> nodeInfoSet) {
        for (NodeInfo nodeInfo : nodeInfoSet) {
            this.nodeInfoMap.put(nodeInfo.getNodeId(), nodeInfo);
        }

    }

    public void addNodeInfoMap(NodeInfo nodeInfo) {
        this.nodeInfoMap.put(nodeInfo.getNodeId(), nodeInfo);
    }

    public void addNodeInfoMap(NodeInfoList nodeInfoList) {
        for (NodeInfo nodeInfo : nodeInfoList.getNodeInfoList()) {
            this.nodeInfoMap.put(nodeInfo.getNodeId(), nodeInfo);
        }
    }

    public NodeInfo getNodeInfo(String nodeId) {
        return this.nodeInfoMap.get(nodeId);
    }

    public void removeNodeInfoMap(String nodeId) {
        this.nodeInfoMap.remove(nodeId);
    }

    public Map<String, NodeInfo> getAllNodeInfoMap() {
        return this.nodeInfoMap;
    }

    public NodeInfoList getAllNodeAsInfoList() {
        NodeInfoList nodeInfoList = new NodeInfoList(nodeInfoMap);
        return nodeInfoList;
    }

    public void clearNodeInfoMap() {
        this.nodeInfoMap.clear();
    }


    // NodesIamConnectedTo
    public void addNodesIamConnectedTo(String nodeId, Connection connection) {
        this.nodesIamConnectedTo.put(nodeId, connection);
    }

    public Optional<Connection> getNodeIamConnectedTo(String nodeId) {
        return Optional.ofNullable(this.nodesIamConnectedTo.get(nodeId));
    }

    public boolean isNodeIamConnectedTo(String nodeId) {
        return this.nodesIamConnectedTo.containsKey(nodeId);
    }

    public void removeNodeIamConnectedTo(String nodeId) {
        this.nodesIamConnectedTo.remove(nodeId);
    }

    public Map<String, Connection> getAllNodesIamConnectedTo() {
        return this.nodesIamConnectedTo;
    }

    public void clearNodesIamConnectedTo() {
        this.nodesIamConnectedTo.clear();
    }


    // NodesConnectedToMe
    public void addNodesConnectedToMe(NodeInfo nodeInfo) {
        this.nodesConnectedToMe.put(nodeInfo.getNodeId(), nodeInfo);
    }

    public Optional<NodeInfo> getNodeConnectedToMe(String nodeId) {
        return Optional.ofNullable(this.nodesConnectedToMe.get(nodeId));
    }

    public boolean isNodeConnectedToMe(String nodeId) {
        return this.nodesConnectedToMe.containsKey(nodeId);
    }

    public void removeNodeConnectedToMe(String nodeId) {
        this.nodesConnectedToMe.remove(nodeId);
    }

    public Map<String, NodeInfo> getAllNodesConnectedToMe() {
        return this.nodesConnectedToMe;
    }

    public void clearNodesConnectedToMe() {
        this.nodesConnectedToMe.clear();
    }


    // MessageHashMap
    public void addMessageHashMap(Message message) {
        this.messageHashMap.put(message.getMessageId(), message);
    }

    public void removeMessageHashMap(String messageId) {
        this.messageHashMap.remove(messageId);
    }

    public Map<String, Message> getAllMessageHashMap() {
        return this.messageHashMap;
    }

    public void clearMessageHashMap() {
        this.messageHashMap.clear();
    }

    public boolean containsMessageHashMap(String messageId) {
        return this.messageHashMap.containsKey(messageId);
    }


    public void setPupularity() {
        double alpha = 1.5;
        double beta = 1.5;
        int newPopularity = (int) ((100 * (alpha * nodesConnectedToMe.size() - beta * nodesIamConnectedTo.size()) / (alpha + beta)) * Math.sqrt(nodesConnectedToMe.size() + nodesIamConnectedTo.size()));
        myNodeInfo.setPopularity(newPopularity);
    }

}
