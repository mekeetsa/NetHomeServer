package nu.nethome.home.items.zwave.messages;

/**
 * 01 20 F9819C1C 01 2F
 */
public class MemoryGetIdResponse extends Event {

    public static final int EXPECTED_LENGTH = 7;
    public final int homeId;
    public final int nodeId;

    public MemoryGetIdResponse(byte[] message) throws DecoderException {
        super(message, MemoryGetIdRequest.MemoryGetId, EXPECTED_LENGTH);
        homeId = (getPayloadInt(0) << 24) + (getPayloadInt(1) << 16) + (getPayloadInt(2) << 8) + getPayloadInt(3);
        nodeId = getPayloadInt(4);
    }

    @Override
    public String toString() {
        return String.format("%s: homeId=%d, nodeId = %d", MemoryGetIdResponse.class.getSimpleName(), homeId, nodeId);
    }
}
