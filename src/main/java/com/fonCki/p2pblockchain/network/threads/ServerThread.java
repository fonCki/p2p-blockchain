package network.threads;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import network.Connection;
import network.NetworkConfig;
import network.model.Message;
import network.model.MessageType;
import network.model.payload.NodeInfoList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import state.AppState;

import static network.model.MessageType.*;

public class ServerThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ServerThread.class.getName());
    int PORT = 9000;
    int PORT_MAX = 65535;
    int MAX_NODE_CONNECTIONS = 8;
    private ServerSocket serverSocket;
    private AppState appState = AppState.getInstance();


    public ServerThread() {
        // Try to find an available port
        int port = PORT; // Starting port
        while (port < PORT_MAX) { // The max value a port can have is 65535
            try {
                serverSocket = new ServerSocket(port);
                //once this action is done, the port is available and also the address
                NetworkConfig.SERVER_PORT = port;
                logger.info("Server is listening on port " + port);
                break;
            } catch (IOException e) {
                port++;
            }
        }
        if (serverSocket == null) {
            logger.error("No available port found");
            throw new RuntimeException("No available port found");
        }
    }

    public void run() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                Connection connection = new Connection(clientSocket);
                if (appState.getAllNodesConnectedToMe().size() >= MAX_NODE_CONNECTIONS) {
                    logger.info("Max connections reached, refusing connection from: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
                    //I refuse the connection and send the node info table
                    Message message = connection.receive();
                    if (message.getMessageType() != WELCOME) {
                        throw new RuntimeException("Expected WELCOME message");
                    }
                    connection.send(new Message(CONNECTION_REFUSED, new NodeInfoList(appState.getAllNodeInfoMap())));
                    connection.close();
                } else {
                    new MessageListenerThread(connection).start();
                }
            } catch (IOException e) {
                logger.error("Error while accepting a new connection");
                e.printStackTrace();
            }
        }
    }

}
