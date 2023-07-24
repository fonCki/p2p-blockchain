package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import network.model.Message;
import network.model.payload.NodeInfo;
import network.model.payload.Payload;
import network.model.payload.PayloadDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connection implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(Connection.class.getName());
    private String connectionId;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private NodeInfo node;
    private Gson gson;

    public Connection(Socket socket) throws IOException {
        this.node = null;
        this.connectionId = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        this.socket = socket;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        this.gson = new Gson();
    }

    public Connection(NodeInfo node) throws IOException {
        this.node = node;
        this.connectionId = node.getIpAddress() + ":" + node.getPort() + ":" + node.getNodeId();
        this.socket = new Socket(node.getIpAddress(), node.getPort());
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        this.gson = new Gson();
    }

    public synchronized void send(Message message) throws IOException {
        String json = gson.toJson(message);
        outputStream.writeInt(json.length());
        outputStream.writeBytes(json);
        outputStream.flush();
    }


    public synchronized Message receive() throws IOException {
            int length = inputStream.readInt();
            byte[] jsonBytes = new byte[length];
            inputStream.readFully(jsonBytes);

            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Payload.class, new PayloadDeserializer());
            Gson gson = gsonBuilder.create();

            Message message = gson.fromJson(new String(jsonBytes), Message.class);
            return message;
        }

        public synchronized void close() throws IOException {
            inputStream.close();
            outputStream.close();
            socket.close();
        }

        public synchronized boolean isClosed () {
            return socket.isClosed();
        }

        public String getConnectionId () {
            return connectionId;
        }

        public NodeInfo getNode () {
            return node;
        }

        public void setNode (NodeInfo node){
            this.node = node;
        }
    }
