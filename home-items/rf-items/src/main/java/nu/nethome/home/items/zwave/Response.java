package nu.nethome.home.items.zwave;

/**
 *
 */
public class Response {

    public static final int Z_WAVE_RESPONSE = 1;
    protected final byte[] message;
    private final byte requestId;

    public static byte decodeRequestId(byte[] message) {
        return (message != null && message.length >= 2) ? message[1] : 0;
    }

    public Response(byte[] message, byte requestId, int expectedLength) throws DecoderException {
        this.message = message;
        this.requestId = requestId;
        if (message.length != expectedLength) throw new DecoderException("Wrong length: " + message.length);
        if (message[0] != Z_WAVE_RESPONSE) throw new DecoderException("Wrong type of message: " + message[0]);
        if (message[1] != requestId) throw new DecoderException("Wrong request id: " + message[1]);
    }

    public byte getRequestId() {
        return requestId;
    }

    public static class DecoderException extends Throwable {
        public DecoderException(String message) {
            super(message);
        }
    }
}
