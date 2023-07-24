package network.model;

import network.model.payload.Payload;
import state.AppState;

import java.util.Objects;
import java.util.UUID;

public class Message {
    private MessageType messageType;
    private final String messageId;
    private final String createdBy;
    private Payload payload;

    // Constructors

    public Message(MessageType messageType) {
        this.messageType = messageType;
        this.messageId = UUID.randomUUID().toString();
        this.payload = null;
        createdBy = AppState.getInstance().getMyNodeInfo().getNodeId();
    }

    public Message(MessageType messageType, String messageId, Payload payload) {
        this.messageType = messageType;
        this.messageId = messageId;
        this.payload = payload;
        createdBy = AppState.getInstance().getMyNodeInfo().getNodeId();
    }

    public Message(MessageType messageType, Payload payload) {
        this.messageType = messageType;
        this.messageId = UUID.randomUUID().toString();
        this.payload = payload;
        createdBy = AppState.getInstance().getMyNodeInfo().getNodeId();
    }


    // Getters and setters
    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getMessageId() {
        return messageId;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageType=" + messageType +
                ", messageId='" + messageId + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", payload=" + payload +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message message)) return false;
        return messageType == message.messageType && Objects.equals(messageId, message.messageId) && Objects.equals(createdBy, message.createdBy) && Objects.equals(payload, message.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageType, messageId, createdBy, payload);
    }
}

