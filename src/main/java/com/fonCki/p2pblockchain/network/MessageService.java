package network;

import network.model.Message;

import network.model.MessageType;
import network.model.payload.NodeInfo;
import network.model.payload.NodeInfoList;
import network.model.payload.TextContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import state.AppState;

import java.io.IOException;
import static network.model.MessageType.*;


/**
 * This class is responsible for processing the messages received by the node
 * actions related with only one connection should be done here
 * actions related with multiple connections should be done in ConnectionManager
 * actions related with the node state should be done in AppState
 * actions related with the network state should be done in NetworkState
 *
 *
 */
public class MessageService {
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class.getName());
    private final Connection connection;
    private AppState appState = AppState.getInstance();
    private ConnectionManager connectionManager = new ConnectionManager();

    public MessageService(Connection connection) {
    this.connection = connection;
    }


    public void processGOSSIP_NEW_NODE(Message message) throws IOException {
        NodeInfo nodeInfo = (NodeInfo) message.getPayload();
        //check if node info is already in the table, if is in the table do nothing, if not add it and gossip to other nodes
        if (!appState.getAllNodeInfoMap().containsKey(nodeInfo.getNodeId())) {
            appState.addNodeInfoMap(nodeInfo);
            connectionManager.gossip(GOSSIP_NEW_NODE, nodeInfo);
        }
    }

    public void processGOSSIP_NODE_DOWN(Message message) throws IOException {
        NodeInfo nodeInfo = (NodeInfo) message.getPayload();
        //check if node was deleted from the table, if it was do nothing, if not delete it and gossip to other nodes
        if (appState.getAllNodeInfoMap().containsKey(nodeInfo.getNodeId())) {
            appState.removeNodeInfoMap(nodeInfo.getNodeId());

            //this 2 should not be necessary. should not be connected to me, and should not be in the node info table
            appState.removeNodeConnectedToMe(nodeInfo.getNodeId());
            appState.removeNodeIamConnectedTo(nodeInfo.getNodeId());

            connectionManager.gossip(GOSSIP_NODE_DOWN, nodeInfo);
        }
    }


    public boolean isMessageDuplicate(Message message) throws IOException{
        if (appState.containsMessageHashMap(message.getMessageId())) {
            connection.send(new Message(DUPLICATE));
            return true;
        } else {
            appState.addMessageHashMap(message);
            return false;
        }
    }

    private boolean isMyMessage(Message message) {
        return message.getCreatedBy().equals(appState.getMyNodeInfo().getNodeId());
    }

    public void processWELCOME(Message message) throws IOException {
        //Steps that I need to do when I receive a welcome message

        //1. I add the node to the node info table, and to the nodes connected to me, and also set the node in the connection
        NodeInfo node = (NodeInfo) message.getPayload();
        logger.info("Connection accepted : {}", node.getNodeId());
        appState.addNodesConnectedToMe(node);
        appState.addNodeInfoMap(node);
        connection.setNode(node);

        //2.As soon as I receive a welcome message, I am not lonely anymore
        if (appState.getMyNodeInfo().getStatus() == NodeInfo.Status.LONELY) {
            appState.setStatusNodeInfo(NodeInfo.Status.CONNECTED);
        }

        //3. I send a connection accepted message to the node
        connection.send(new Message(CONNECTION_ACCEPTED, appState.getMyNodeInfo()));

        //4. I send the node info table to the node
        if (node.getStatus() == NodeInfo.Status.LONELY && appState.getAllNodesConnectedToMe().size() < appState.MAX_NODE_CONNECTIONS) {
            connectionManager.connect(node);
        }

        //5. I gossip the new node to the network
        connectionManager.gossip(GOSSIP_NEW_NODE, node);
    }

    public void processCONNECTION_REFUSED(Message message) throws IOException {
        NodeInfo node = (NodeInfo) message.getPayload();
        appState.addNodeInfoMap(node);
        connection.send(new Message(NODE_INFO_TABLE, new NodeInfoList(appState.getAllNodeInfoMap())));
    }

    public void processUSER_MESSAGE(Message message) throws IOException {
        logger.warn("MESSAGE: " + ((TextContent) message.getPayload()).getText());
        connection.send(new Message(ACKNOWLEDGE, new TextContent(message.getMessageId())));
    }

    public void processREQUEST_NODE_INFO_TABLE(Message message) throws IOException {
        connection.send(new Message(NODE_INFO_TABLE, new NodeInfoList(appState.getAllNodeInfoMap())));
    }

    public void processBROADCAST(Message message) throws IOException {
        logger.warn("MESSAGE: " + ((TextContent) message.getPayload()).getText());
        connection.send(new Message(ACKNOWLEDGE, new TextContent(message.getMessageId())));
        if (!isMyMessage(message)) {
            connectionManager.broadcastUserMessage(message);
        }
    }

    public void processDEFAULT(Message message) throws IOException {
    }

}
