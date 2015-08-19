package nu.nethome.home.items.zwave.messages.commands;

import nu.nethome.home.items.zwave.messages.DecoderException;

import java.io.ByteArrayOutputStream;

/**
 * Configuration command class is used to read and write configuration parameters in nodes
 */
public class Configuration implements CommandClass {

    private static final int SET_CONFIGURATION = 0x04;
    private static final int GET_CONFIGURATION = 0x05;
    private static final int REPORT_CONFIGURATION = 0x06;

    public static final int COMMAND_CLASS = 0x70;

    public static class Get extends CommandAdapter {
        public final int configurationId;

        public Get(int configurationId) {
            super(COMMAND_CLASS, GET_CONFIGURATION);
            this.configurationId = configurationId;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(configurationId);
        }
    }

    public static class Set extends CommandAdapter {
        public final int configurationId;
        public final Parameter parameter;

        public Set(int configurationId, Parameter parameter) {
            super(COMMAND_CLASS, GET_CONFIGURATION);
            this.configurationId = configurationId;
            this.parameter = parameter;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(configurationId);
            parameter.write(result);
        }
    }

    public static class Report extends CommandAdapter {
        public final int configurationId;
        public final Parameter parameter;

        public Report(byte[] data) throws DecoderException {
            super(data, COMMAND_CLASS, REPORT_CONFIGURATION);
            configurationId = in.read();
            parameter = new Parameter(in);
        }

        public static class Processor extends CommandProcessorAdapter<Report> {
            @Override
            public Report process(byte[] command) throws DecoderException {
                return process(new Report(command));
            }
        }

        @Override
        public String toString() {
            return String.format("Parameter.Report{parameter:%d, value:%s", configurationId, parameter.toString());
        }
    }
}
