package network;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import network.model.Message;
import network.model.MessageType;
import network.model.payload.NodeInfo;
import network.model.payload.NodeInfoList;
import network.model.payload.TextContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import state.AppState;

import static network.model.MessageType.*;

public class ConnectionManager {
    private AppState appState = AppState.getInstance();
    private Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    private final NodeInfo masterNodeInfo = new NodeInfo(
                    "MASTER_NODE",
                    "192.168.1.126",
                    9000);

    public Optional<Connection> connect(NodeInfo node) {
        //always check if connection already exists or if it is a connection to myself
        //TODO: check how to avoid connect to myself
        if (appState.getAllNodesIamConnectedTo().containsKey(node.getNodeId()) ||
                node.getNodeId().equals(appState.getMyNodeInfo().getNodeId()) ||
                (((node.getIpAddress().equals(appState.getMyNodeInfo().getIpAddress()) &&
                        node.getPort() == appState.getMyNodeInfo().getPort())))) {
            return Optional.empty();
        }
        try {
            Connection connection = new Connection(node);
            Message initialMessage = new Message(WELCOME, appState.getMyNodeInfo().getNodeId(), appState.getMyNodeInfo());
            connection.send(initialMessage);
            Message replyMessage = connection.receive();

            if (replyMessage.getMessageType() == CONNECTION_ACCEPTED) {
                NodeInfo nodeInfoFromReply = (NodeInfo) replyMessage.getPayload();
                logger.info("Connection accepted by {}", nodeInfoFromReply.getNodeId());
                appState.addNodesIamConnectedTo(nodeInfoFromReply.getNodeId(), connection);
                appState.addNodeInfoMap((NodeInfo) replyMessage.getPayload());
                return Optional.of(connection);
            } else if (replyMessage.getMessageType() == CONNECTION_REFUSED) {
                //if the connection is refused, I still add the node to my node info table
                logger.info("Connection refused by {}", node.getNodeId());
                appState.addNodeInfoMap((NodeInfoList) replyMessage.getPayload());
                connection.close();
            }
            return Optional.empty();
        } catch (IOException e) {
            logger.error("Connection failed to {}", node.getNodeId());
            return Optional.empty();
        }
    }

    public void initConnection() {
        connect(masterNodeInfo).ifPresent(this::requestNodeInfoTable);
    }

    public void requestNodeInfoTable(Connection connection) {
        try {
            connection.send(new Message(REQUEST_NODE_INFO_TABLE));
            Message reply = connection.receive();
            if (reply.getMessageType() == NODE_INFO_TABLE) {
                NodeInfoList nodeInfoTableFetched = (NodeInfoList) reply.getPayload();
                appState.addNodeInfoMap(nodeInfoTableFetched.getNodeInfoList());
            } else {
                logger.error("Received unexpected message type: " + reply.getMessageType());
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public void establishConnections() {
        // Iterate over all nodes in nodeInfoTable
        List<NodeInfo> sortedNodeInfoList = appState.getAllNodeAsInfoList().getNodeInfoList();
        //TODO the problem that I have here is that even the table is updated, the node isn't
        //to avoid rewritiing, the nodes not rewrite a node that is already in the list, in this way, the avoid inncessary connections

        //sortedNodeInfoList.sort(Comparator.comparingInt(node -> node.getPopularity()));
        Collections.shuffle(sortedNodeInfoList);
        //the less popular nodes are at the beginning of the list

        //sort the list by node the ammonut of connections
        for (NodeInfo nodeInfo : sortedNodeInfoList) {
            connect(nodeInfo).ifPresent(this::requestNodeInfoTable);
            //if I have reached the max number of connections, stop
            //I stop at MAX_NODE_CONNECTIONS - 2 because I have not nodes connected to me yet, and I want to leave 2 connections available for a lonely node
            if (appState.getAllNodesIamConnectedTo().size() >= (appState.MAX_NODE_CONNECTIONS - 2))  {
                break;
            }
        }
    }

    public void disconnect(Connection connection) {
        try {
            connection.close();
        } catch (IOException e) {
            logger.error("Failed to disconnect: {}", e.getMessage());
        }
    }

    public void broadcastUserMessage(Message message) {
        logger.info("Sending message to all connected nodes : " + appState.getAllNodesIamConnectedTo().size() + " nodes");

        for (Map.Entry<String, Connection> entry : appState.getAllNodesIamConnectedTo().entrySet()) {
            String nodeId = entry.getKey();
            NodeInfo node = appState.getNodeInfo(nodeId);
            Connection connection = entry.getValue();

            logger.info("Sending message to " + nodeId);
            logger.info("Connection: " + connection.getConnectionId());
            try {
                connection.send(message);
                Message response = connection.receive();
                logger.info("Received {} message from {}", response.getMessageType(), nodeId);
            } catch (IOException e) {
                checkConnection(connection);
                logger.error(e.getMessage());
            }
        }
    }

    public void broadcastUserMessage(String userInput) {
        // Create a new Message object
        Message userMessage = new Message(BROADCAST, new TextContent(userInput));
        broadcastUserMessage(userMessage);
    }

    public void gossip(MessageType type, NodeInfo node) {
        //Max number of nodes to gossip is equal to the total of I am connected to divided by 3, but minimum 2
        int maxGossipConnections = Math.max(appState.getAllNodesIamConnectedTo().size() / 3, 2);
        // get 3 random nodes from the list of nodes connected to
        // and send them the node info
        List<Connection> connections = new ArrayList<>(appState.getAllNodesIamConnectedTo().values());
        Collections.shuffle(connections);
        connections.stream()
                .limit(maxGossipConnections)
                .forEach(connection -> {
                    try {
                        connection.send(new Message(type, node));
                        //sleep for random 2~5 seconds to avoid flooding the network
                        Thread.sleep((long) (Math.random() * 3000 + 2000));
                    } catch (IOException | InterruptedException e) {
                        checkConnection(connection);
                        logger.error(e.getMessage());
                    }
                });

    }


    public void checkConnection(Connection connection) {

        final int MAX_TRIES = 3;
        ExecutorService executor = Executors.newFixedThreadPool(MAX_TRIES);
        final int TIME_BETWEEN_TRIES = 5;

        if (connection == null) {
            return;
        }
        NodeInfo node = connection.getNode();
        executor.submit(() -> {
            for (int i = 0; i < MAX_TRIES; i++) {
                try {
                    connection.send(new Message(PING));
                    Message response = connection.receive();
                    if (response.getMessageType() == ACKNOWLEDGE) {
                        logger.info("Received PING_ACKNOWLEDGE from {}", node.getNodeId());
                        return; // the node is still alive, exit the method
                    }
                } catch (IOException e) {
                    logger.error("Failed to send or receive message from {}: {}", node.getNodeId(), e.getMessage());
                }

                // Sleep for TIME_BETWEEN_TRIES seconds before the next try
                try {
                    TimeUnit.SECONDS.sleep(TIME_BETWEEN_TRIES);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Sleep between tries was interrupted: {}", e.getMessage());
                }
            }
            clearConnection(node, connection);
            executor.shutdown();
        });
    }

    private void clearConnection(NodeInfo node, Connection connection) {
        logger.warn("Cleaning any traces of connection to {}", node.getNodeId());
        logger.warn("Closing connection to {}", node.getNodeId());
        logger.warn("Removing node from node info table, nodes connected to me, and nodes I am connected to");
        try {
            connection.close();
        } catch (IOException e) {
            logger.error("Failed to close connection to {}: {}", node.getNodeId(), e.getMessage());
            connection = null;
        }
        appState.removeNodeConnectedToMe(node.getNodeId());
        appState.removeNodeIamConnectedTo(node.getNodeId());
        appState.removeNodeInfoMap(node.getNodeId());
        gossip(GOSSIP_NODE_DOWN, node);
    }


}

