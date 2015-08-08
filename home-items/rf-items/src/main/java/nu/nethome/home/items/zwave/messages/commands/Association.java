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

    public final int command;
    public final int associationId;
    public final int maxAssociations;
    public final int reportsToFollow;
    public final int[] nodes;

    private Association(int command, int associationId) {
        this.command = command;
        this.associationId = associationId;
        maxAssociations = 0;
        reportsToFollow = 0;
        nodes = new int[0];
    }

    public Association(int command, int associationId, int maxAssociations, int reportsToFollow, int[] nodes) {
        this.command = command;
        this.associationId = associationId;
        this.maxAssociations = maxAssociations;
        this.reportsToFollow = reportsToFollow;
        this.nodes = nodes;
    }

    // 00 04 0006 06: 85 03 02 0A 00 02
    public static Association decodeReport(ByteArrayInputStream data) throws DecoderException {
        int length = data.read();
        DecoderException.assertTrue(data.read() == COMMAND_CLASS, "Wrong command class in Association");
        int command = data.read();
        if (command == ASSOCIATION_REPORT) {
            int associationId = data.read();
            int maxAssociations = data.read();
            int reportsToFollow = data.read();
            int numberOfNodes = length - 5;
            int[] nodes = new int[numberOfNodes];
            for (int i = 0; i < numberOfNodes; i++) {
                nodes[i] = data.read();
            }
            return new Association(COMMAND_CLASS, associationId, maxAssociations, reportsToFollow, nodes);
        } else {
            throw new DecoderException("Unsupported Command");
        }
    }

    public static Association getAssociation(int associationId) {
        Association result = new Association(GET_ASSOCIATION, associationId);
        return result;
    }

    public static Association reportAssociations() {
        Association result = new Association(ASSOCIATION_REPORT, 0);
        return result;
    }

    @Override
    public byte[] encode() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        result.write(COMMAND_CLASS);
        result.write(command);
        if (command != REPORT_GROUPINGS) {
            result.write(associationId);
        }
        return result.toByteArray();
    }
}
