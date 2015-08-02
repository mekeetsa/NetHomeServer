package nu.nethome.home.items.zwave.messages;

import nu.nethome.home.items.zwave.messages.commands.ApplicationCommand;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 */
public class ApplicationCommandRequest extends Request {

    public static final byte REQUEST_ID = (byte) 0x04;
    public final byte node;
    public final ApplicationCommand command;

    public ApplicationCommandRequest(byte node, ApplicationCommand command) {
        super(REQUEST_ID);
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
