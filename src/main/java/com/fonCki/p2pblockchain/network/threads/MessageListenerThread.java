package network.threads;

import network.Connection;
import network.ConnectionManager;
import network.MessageService;
import network.model.Message;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import org.slf4j.Logger;


public class MessageListenerThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(MessageListenerThread.class.getName());
    Connection connection;
    MessageService messageService;


    public MessageListenerThread(Connection connection) {
        this.connection = connection;
        this.messageService = new MessageService(connection);
    }

    public void run() {
        try {
            while (true) {
                Message message = connection.receive();
                if (!messageService.isMessageDuplicate(message)) {
                    logger.info("Received message type: " + message.getMessageType());
                    logger.info(message.toString());
                    switch (message.getMessageType()) {
                        case WELCOME: {
                            messageService.processWELCOME(message);
                            break;
                        }
                        case USER_MESSAGE: {
                            messageService.processUSER_MESSAGE(message);
                            break;
                        }
                        case REQUEST_NODE_INFO_TABLE: {
                            messageService.processREQUEST_NODE_INFO_TABLE(message);
                            break;
                        }
                        case BROADCAST: {
                            messageService.processBROADCAST(message);
                            break;
                        }
                        case CONNECTION_REFUSED: {
                            messageService.processCONNECTION_REFUSED(message);
                            break;
                        }
                        case GOSSIP_NEW_NODE: {
                            messageService.processGOSSIP_NEW_NODE(message);
                            break;
                        }
                        case GOSSIP_NODE_DOWN: {
                            messageService.processGOSSIP_NODE_DOWN(message);
                            break;
                        }
                        default: {
                            messageService.processDEFAULT(message);
                            break;
                        }
                    }

                }
            }
        } catch (IOException e) {
            new ConnectionManager().checkConnection(connection);
        }
    }
}


