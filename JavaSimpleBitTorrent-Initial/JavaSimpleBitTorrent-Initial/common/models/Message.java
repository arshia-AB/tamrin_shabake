package common.models;

import java.util.HashMap;

public class Message {
    private Type type;
    private HashMap<String, Object> body;

    /*
     * Empty constructor needed for JSON Serialization/Deserialization
     */
    public Message() {
    }

    public Message(HashMap<String, Object> body, Type type) {
        this.body = body;
        this.type = type;
    }

    public static Message createCommand(String commandName) {
        HashMap<String, Object> body = new HashMap<>();
        body.put("command", commandName);
        return new Message(body, Type.command);
    }

    public static Message createResponse(String commandName, String response) {
        HashMap<String, Object> body = new HashMap<>();
        body.put("command", commandName);
        body.put("response", response);
        return new Message(body, Type.response);
    }

    public Type getType() {
        return type;
    }

    public <T> T getFromBody(String fieldName) {
        return (T) body.get(fieldName);
    }

    public int getIntFromBody(String fieldName) {
        return (int) ((double) ((Double) body.get(fieldName)));
    }

    public enum Type {
        command,
        response,
        file_request,
        download_request
    }
}
