package nu.nethome.home.items.zwave.messages.commands;

import nu.nethome.home.items.zwave.messages.DecoderException;

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
public class SwitchBinary implements CommandClass {

    private static final int SWITCH_BINARY_SET = 0x01;
    private static final int SWITCH_BINARY_GET = 0x02;
    private static final int SWITCH_BINARY_REPORT = 0x03;

    public static final byte COMMAND_CLASS = (byte) 0x25;

    public static class Set extends CommandAdapter {
        public final boolean isOn;

        public Set(boolean on) {
            super(COMMAND_CLASS, SWITCH_BINARY_SET);
            isOn = on;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(isOn ? 0xFF : 0);
        }
    }

    public static class Get extends CommandAdapter {
        public Get() {
            super(COMMAND_CLASS, SWITCH_BINARY_GET);
        }
    }

    public static class Report extends CommandAdapter {
        public final boolean isOn;

        public Report(byte[] data) throws DecoderException {
            super(data, COMMAND_CLASS, SWITCH_BINARY_REPORT);
            isOn = (in.read() > 0);
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(isOn ? 0xFF : 0);
        }

        public static class Processor extends CommandProcessorAdapter<Report> {
            @Override
            public Report process(byte[] command) throws DecoderException {
                return process(new Report(command));
            }
        }
    }
}
