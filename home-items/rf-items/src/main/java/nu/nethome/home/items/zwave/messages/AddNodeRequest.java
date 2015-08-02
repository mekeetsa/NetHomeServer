package nu.nethome.home.items.zwave.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 */
public class AddNodeRequest extends Request {

    private InclusionMode inclusionMode;

    public enum InclusionMode {
        ADD_NODE_ANY(0x01),
        ADD_NODE_CONTROLLER(0x02),
        ADD_NODE_SLAVE(0x03),
        ADD_NODE_EXISTING(0x04),
        ADD_NODE_STOP(0x05),
        ADD_NODE_STOP_FAILED(0x06);

        private byte value;
        InclusionMode(int value) {
            this.value = (byte)value;
        }

        public byte getValue() {
            return value;
        }
    }

    public static final byte REQUEST_ID = (byte) 0x4a;

    public AddNodeRequest(InclusionMode inclusionMode) {
        super(REQUEST_ID);
        this.inclusionMode = inclusionMode;
    }

    @Override
    protected void addRequestData(ByteArrayOutputStream result) throws IOException {
        super.addRequestData(result);
        result.write(inclusionMode.getValue());
        result.write(0xFF);
    }
}
