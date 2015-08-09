package nu.nethome.home.items.zwave.messages;

import nu.nethome.home.items.zwave.messages.commands.Command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 */
public class ApplicationCommand {

    public static final byte REQUEST_ID = (byte) 0x04;

    public static class Request extends Message {
        public final byte node;
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
            result.write(node);
            result.write(commandData.length);
            result.write(commandData);
        }
    }
}
