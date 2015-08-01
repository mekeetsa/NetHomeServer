package nu.nethome.home.items.zwave;

/**
 * 01 20 F9819C1C 01 2F
 */
public class MemoryGetIdResponse extends Response {

    public static final int EXPECTED_LENGTH = 7;
    public final int homeId;
    public final int nodeId;

    public MemoryGetIdResponse(byte[] message) throws DecoderException {
        super(message, MemoryGetIdRequest.MemoryGetId, EXPECTED_LENGTH);
        homeId = (asInt(message[2]) << 24) + (asInt(message[3]) << 16) + (asInt(message[4]) << 8) + asInt(message[5]);
        nodeId = asInt(message[6]);
    }

    private int asInt(byte b) {
        return ((int)b) & 0xFF;
    }

    @Override
    public String toString() {
        return String.format("%s: homeId=%d, nodeId = %d", MemoryGetIdResponse.class.getSimpleName(), homeId, nodeId);
    }
}
