package network.model.payload;

public class TextContent extends Payload{
    private String text;

    public TextContent(String text) {
        super(Type.TEXT_CONTENT);
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
