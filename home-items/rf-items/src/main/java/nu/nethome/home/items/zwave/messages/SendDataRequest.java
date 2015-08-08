package nu.nethome.home.items.zwave.messages;

import nu.nethome.home.items.zwave.messages.commands.CommandClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 */
public class SendDataRequest extends Request {

    public static final int TRANSMIT_OPTION_ACK = 0x01;
    public static final int TRANSMIT_OPTION_AUTO_ROUTE = 0x04;
    public static final int TRANSMIT_OPTION_EXPLORE = 0x20;

    public static final byte REQUEST_ID = (byte) 0x13;
    public final byte node;
    public final CommandClass command;
    public final int transmitOptions;
    public final int callbackId;
    private static int nextCallbackId = 0;


    public SendDataRequest(byte node, CommandClass command, int transmitOptions) {
        super(REQUEST_ID);
        this.node = node;
        this.command = command;
        this.transmitOptions = transmitOptions;
        callbackId = nextCallbackId;
        nextCallbackId = (nextCallbackId + 1) & 0xFF;
    }

    @Override
    protected void addRequestData(ByteArrayOutputStream result) throws IOException {
        byte[] commandData = command.encode();
        super.addRequestData(result);
        result.write(node);
        result.write(commandData.length);
        result.write(commandData);
        result.write(transmitOptions);
        result.write(callbackId);
    }
}
