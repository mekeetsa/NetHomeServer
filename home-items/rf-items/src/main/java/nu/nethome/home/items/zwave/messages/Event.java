package nu.nethome.home.items.zwave.messages;

/**
 *
 */
public class Event {
    protected final byte[] message;
    private final byte requestId;
    private final boolean isRequest;

    public static byte decodeRequestId(byte[] message) {
        return (message != null && message.length >= 2) ? message[1] : 0;
    }

    public Event(byte[] message, byte requestId, int expectedLength) throws DecoderException {
        this.message = message;
        this.requestId = requestId;
        if (message.length != expectedLength) throw new DecoderException("Wrong length: " + message.length);
        isRequest = (message[0] == 0);
        if (message[1] != requestId) throw new DecoderException("Wrong request id: " + message[1]);
    }

    public byte getRequestId() {
        return requestId;
    }

    protected byte getPayloadByte(int number) {
        return message[number + 2];
    }

    protected int getPayloadInt(int number) {
        return ((int)getPayloadByte(number)) & 0xFF;
    }
}
