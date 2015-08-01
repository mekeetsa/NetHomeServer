package nu.nethome.home.items.zwave.messages;

/**
 *
 */
public class AddNodeEvent extends Event {

    // 004AFF010000

    private static final int EXPECTED_LENGTH = 6;
    public final Status status;
    public final int nodeId;

    public enum Status {
        LEARN_READY(0x01),
        NODE_FOUND(0x02),
        ADDING_SLAVE(0x03),
        ADDING_CONTROLLER(0x04),
        PROTOCOL_DONE(0x05),
        DONE(0x06),
        FAILED(0x07);

        private byte value;
        Status(int value) {
            this.value = (byte)value;
        }

        public byte getValue() {
            return value;
        }

        public static Status fromValue(byte value) throws DecoderException {
            for (Status status : Status.values()) {
                if (status.getValue() == value) {
                    return status;
                }
            }
            throw new DecoderException("Unknown value");
        }
    }

    public AddNodeEvent(byte[] message) throws DecoderException {
        super(message, AddNodeRequest.REQUEST_ID, EXPECTED_LENGTH);
        status = Status.fromValue(getPayloadByte(1));
        nodeId = getPayloadInt(2);
    }

    @Override
    public String toString() {
        return String.format("%s: status: %s, node: %d", AddNodeEvent.class.getSimpleName(), status.name(), nodeId);
    }
}
