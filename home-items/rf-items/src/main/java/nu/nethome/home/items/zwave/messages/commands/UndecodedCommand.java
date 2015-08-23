package nu.nethome.home.items.zwave.messages.commands;

import nu.nethome.home.items.zwave.Hex;
import nu.nethome.home.items.zwave.messages.DecoderException;

import java.util.Arrays;

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

    public static class Processor extends CommandProcessorAdapter<UndecodedCommand> {
        @Override
        public UndecodedCommand process(byte[] command, int node) throws DecoderException {
            return process(new UndecodedCommand(command));
        }
    }

    @Override
    public String toString() {
        return String.format("Command[%02X.%02X]{%s}", getCommandClass(), getCommand(), Hex.asHexString(Arrays.copyOfRange(commandData, 2, commandData.length)));
    }
}
