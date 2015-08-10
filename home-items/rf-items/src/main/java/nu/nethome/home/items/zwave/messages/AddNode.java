package nu.nethome.home.items.zwave.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddNode {

    public static final byte REQUEST_ID = (byte) 0x4a;

    public static class Request extends MessageAdaptor {
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

        public Request(InclusionMode inclusionMode) {
            super(REQUEST_ID, Type.REQUEST);
            this.inclusionMode = inclusionMode;
        }

        @Override
        protected void addRequestData(ByteArrayOutputStream result) throws IOException {
            super.addRequestData(result);
            result.write(inclusionMode.getValue());
            result.write(0xFF); // ??
        }
    }

    public static class Event extends MessageAdaptor {
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

        public Event(byte[] message) throws DecoderException {
            super(message, REQUEST_ID, Type.REQUEST);
            in.read(); // ??
            status = Status.fromValue((byte) in.read());
            nodeId = in.read();
        }

        @Override
        public String toString() {
            return String.format("%s: status: %s, node: %d", Event.class.getSimpleName(), status.name(), nodeId);
        }
    }
}
