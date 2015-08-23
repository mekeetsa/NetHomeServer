package nu.nethome.home.items.zwave.messages;

import nu.nethome.home.items.zwave.messages.commands.Command;
import nu.nethome.home.items.zwave.messages.commands.CommandProcessor;
import nu.nethome.home.items.zwave.messages.commands.MultiCommandProcessor;
import nu.nethome.home.items.zwave.messages.commands.UndecodedCommand;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 */
public class ApplicationCommand {

    public static final byte REQUEST_ID = (byte) 0x04;

    public static class Request extends MessageAdaptor {
        public final int node;
        public final Command command;

        public Request(byte node, Command command) {
            super(REQUEST_ID, Type.REQUEST);
            this.node = node;
            this.command = command;
        }

        @Override
        protected void addRequestData(ByteArrayOutputStream result) throws IOException {
            byte[] commandData = command.encode();
            super.addRequestData(result);
            result.write(0);
            result.write(node);
            result.write(commandData.length);
            result.write(commandData);
        }

        public Request(byte[] data) throws IOException, DecoderException {
            this(data, new CommandProcessor() {
                @Override
                public Command process(byte[] commandData, int node) throws DecoderException {
                    return new UndecodedCommand(commandData);
                }
            });
        }

        public Request(byte[] data, CommandProcessor processor) throws IOException, DecoderException {
            super(data, REQUEST_ID, Type.REQUEST);
            in.read(); // ?? Seems to be zero
            node = in.read();
            int commandLength = in.read();
            byte[] commandData = new byte[commandLength];
            in.read(commandData);
            command = processor.process(commandData, node);
        }

        @Override
        public String toString() {
            return String.format("ApplicationCommand.Request(node:%d, command:{%s})", node, command.toString());
        }

        public static class Processor extends MessageProcessorAdaptor<Request> {

            private CommandProcessor commandProcessor;

            public Processor(CommandProcessor commandProcessor) {
                this.commandProcessor = commandProcessor;
            }

            public Processor() {
                this.commandProcessor = new MultiCommandProcessor();
            }

            @Override
            public Message process(byte[] message) throws DecoderException, IOException {
                return process(new Request(message, commandProcessor));
            }
        }
    }
}
