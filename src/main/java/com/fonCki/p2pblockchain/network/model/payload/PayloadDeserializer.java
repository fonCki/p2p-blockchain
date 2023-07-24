package network.model.payload;

import com.google.gson.*;

import java.lang.reflect.Type;

public class PayloadDeserializer implements JsonDeserializer<Payload> {
    @Override
    public Payload deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        if (jsonObject.get("type").getAsString().equals(Payload.Type.NODE_INFO.toString())) {
            return context.deserialize(jsonObject, NodeInfo.class);
        }

        if (jsonObject.get("type").getAsString().equals(Payload.Type.NODE_INFO_LIST.toString())) {
            return context.deserialize(jsonObject, NodeInfoList.class);
        }

        if (jsonObject.get("type").getAsString().equals(Payload.Type.TEXT_CONTENT.toString())) {
            return context.deserialize(jsonObject, TextContent.class);
        }
        // Check for other subclasses here
        return null;
    }
}
