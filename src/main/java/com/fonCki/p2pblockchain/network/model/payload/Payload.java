package network.model.payload;

public abstract class Payload {

    public enum Type {
        NODE_INFO,
        NODE_INFO_LIST,
        TEXT_CONTENT,

    }
    protected Type type;
    public Payload(Type type) {
        this.type = type;
    }
    public Type getType() {
        return type;
    }

}
