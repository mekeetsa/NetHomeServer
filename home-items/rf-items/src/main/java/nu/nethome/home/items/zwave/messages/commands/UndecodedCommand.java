package nu.nethome.home.items.zwave.messages.commands;

import nu.nethome.home.items.zwave.messages.DecoderException;

public class UndecodedCommand extends CommandAdapter {
    byte[] commandData;
    public UndecodedCommand(byte[] commandData) throws DecoderException {
        super(commandData);
        this.commandData = commandData;
    }

    @Override
    public byte[] encode() {
        return commandData;
    }
}
