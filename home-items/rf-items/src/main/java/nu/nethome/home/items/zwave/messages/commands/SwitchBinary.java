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
public class SwitchBinary implements ApplicationCommand {

    private static final int SWITCH_BINARY_SET = 0x01;
    private static final int SWITCH_BINARY_GET = 0x02;
    private static final int SWITCH_BINARY_REPORT = 0x03;

    public static final byte COMMAND_CLASS = (byte) 0x25;

    public final boolean isOn;
    public final int command;

    private SwitchBinary(int command, boolean on) {
        this.command = command;
        isOn = on;
    }

    public static SwitchBinary doSwitch(boolean on) {
        SwitchBinary result = new SwitchBinary(SWITCH_BINARY_SET, on);
        return result;
    }

    public static SwitchBinary report() {
        SwitchBinary result = new SwitchBinary(SWITCH_BINARY_REPORT, false);
        return result;
    }

    @Override
    public byte[] encode() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        result.write(COMMAND_CLASS);
        result.write(command);
        if (command == SWITCH_BINARY_SET) {
            result.write(isOn ? 0xFF : 0);
        }
        return result.toByteArray();
    }
}
