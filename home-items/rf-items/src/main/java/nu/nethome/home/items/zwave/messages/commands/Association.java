package nu.nethome.home.items.zwave.messages.commands;

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
public class Association implements ApplicationCommand {

    private static final int ASSOCIATIONCMD_SET = 0x01;
    private static final int ASSOCIATIONCMD_GET = 0x02;
    private static final int ASSOCIATIONCMD_REPORT = 0x03;
    private static final int ASSOCIATIONCMD_REMOVE = 0x04;
    private static final int ASSOCIATIONCMD_GROUPINGSGET = 0x05;
    private static final int ASSOCIATIONCMD_GROUPINGSREPORT = 0x06;

    public static final byte COMMAND_CLASS = (byte) 0x85;

    public final int command;
    public final int associationId;

    private Association(int command, int associationId) {
        this.command = command;
        this.associationId = associationId;
    }

    public static Association getAssociation(int associationId) {
        Association result = new Association(ASSOCIATIONCMD_GET, associationId);
        return result;
    }

    public static Association reportAssociations() {
        Association result = new Association(ASSOCIATIONCMD_REPORT, 0);
        return result;
    }

    @Override
    public byte[] encode() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        result.write(COMMAND_CLASS);
        result.write(command);
        if (command != ASSOCIATIONCMD_GROUPINGSREPORT) {
            result.write(associationId);
        }
        return result.toByteArray();
    }
}
