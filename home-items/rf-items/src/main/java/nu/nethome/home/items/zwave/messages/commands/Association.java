package nu.nethome.home.items.zwave.messages.commands;

import nu.nethome.home.items.zwave.messages.DecoderException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * 004AFF060000
 * 004AFF010000
 * 00 04 0002 06: 31 05 0422000B
 * 00 04 0006 07: 60 0D 0007200100
 * 00 04   06 03: 85 02 09
 *
 * 00 04 0006 02: 84 07
 * 00 04 0006 03: 80 0364
 * 00 04 0006 03: 80 0364
 */
public class Association implements CommandClass {

    private static final int SET_ASSOCIATION = 0x01;
    private static final int GET_ASSOCIATION = 0x02;
    private static final int ASSOCIATION_REPORT = 0x03;
    private static final int REMOVE_ASSOCIATION = 0x04;
    private static final int GET_GROUPINGS = 0x05;
    private static final int REPORT_GROUPINGS = 0x06;

    public static final byte COMMAND_CLASS = (byte) 0x85;

    public static class Get extends CommandAdaptor {
        public final int associationId;

        public Get(int associationId) {
            super(COMMAND_CLASS, GET_ASSOCIATION);
            this.associationId = associationId;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(associationId);
        }
    }

    public static class GetGroupings extends CommandAdaptor {

        public GetGroupings() {
            super(COMMAND_CLASS, GET_GROUPINGS);
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
        }
    }

    public static class Report extends CommandAdaptor {
        public final int associationId;
        public final int maxAssociations;
        public final int reportsToFollow;
        public final int[] nodes;

        public Report(byte[] data, int length) throws DecoderException {
            super(data, COMMAND_CLASS, ASSOCIATION_REPORT);
            associationId = in.read();
            maxAssociations = in.read();
            reportsToFollow = in.read();
            int numberOfNodes = length - 5;
            nodes = new int[numberOfNodes];
            for (int i = 0; i < numberOfNodes; i++) {
                nodes[i] = in.read();
            }
        }
    }
    // 00 04 0006 06: 85 03 02 0A 00 02
}
