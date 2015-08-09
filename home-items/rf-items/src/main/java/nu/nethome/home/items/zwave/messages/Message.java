package nu.nethome.home.items.zwave.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Message {
    public static byte decodeMessageId(byte[] message) {
        return (message != null && message.length >= 2) ? message[1] : 0;
    }

    public enum Type {REQUEST, RESPONSE};
    public static final byte Z_WAVE_REQUEST = 0;
    private final int requestId;
    private final Type type;

    public Message(int requestId, Type type) {
        this.requestId = requestId;
        this.type = type;
    }

    public Message(ByteArrayInputStream in, int requestId, Type type) throws DecoderException {
        this.requestId = requestId;
        this.type = type;
        DecoderException.assertTrue(in.read() == (type == Type.REQUEST ? 0 : 1), "Unexpected message type");
        DecoderException.assertTrue(in.read() == requestId, "Unexpected message type");
    }

    public byte[] encode() {
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            addRequestData(result);
            return result.toByteArray();
        } catch (IOException e) {
            // This should not happen
            return new byte[0];
        }
    }

    protected void addRequestData(ByteArrayOutputStream result) throws IOException {
        result.write(Z_WAVE_REQUEST);
        result.write(requestId);
    }
}
