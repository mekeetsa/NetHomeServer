package nu.nethome.home.items.zwave.messages;

/**
 *
 */
public interface Message {
    public enum Type {REQUEST, RESPONSE}

    int getRequestId();
    Type getType();

    byte[] encode();
}
