package nu.nethome.home.items.zwave.messages;

import java.io.ByteArrayInputStream;

/*
 * event,ZWave_Message,Direction,Out,Value,0020
 */
public class MemoryGetId  {

    public static final byte MEMORY_GET_ID = (byte) 0x20;

    public static class Request extends MessageAdaptor {
        public Request() {
            super(MEMORY_GET_ID, Type.REQUEST);
        }
    }

    public static class Response extends MessageAdaptor {

        public static final int EXPECTED_LENGTH = 7;
        public final int homeId;
        public final int nodeId;

        public Response(byte[] message) throws DecoderException {
            super(message, MEMORY_GET_ID, Type.RESPONSE);
            homeId = (in.read() << 24) + (in.read() << 16) + (in.read() << 8) + in.read();
            nodeId = in.read();
        }

        @Override
        public String toString() {
            return String.format("%s: homeId=%d, nodeId = %d", MemoryGetId.Response.class.getSimpleName(), homeId, nodeId);
        }
    }
}
