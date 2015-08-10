package nu.nethome.home.items.zwave.messages.commands;

import nu.nethome.home.items.zwave.messages.DecoderException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public abstract class CommandAdaptor implements Command {
    private final int commandClass;
    private final int command;
    protected ByteArrayInputStream in;

    @Override
    public byte[] encode() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        addCommandData(result);
        return result.toByteArray();
    }

    protected void addCommandData(ByteArrayOutputStream result) {
        result.write(commandClass);
        result.write(command);
    }

    protected CommandAdaptor(int commandClass, int command) {
        this.commandClass = commandClass;
        this.command = command;
    }

    protected CommandAdaptor(byte[] commandData, int commandClass, int command) throws DecoderException {
        this(commandClass, command);
        in = new ByteArrayInputStream(commandData);
        DecoderException.assertTrue(in.read() == commandClass, "Wrong command class in Association");
        DecoderException.assertTrue(in.read() == command, "Wrong command class in Association");
    }

    @Override
    public int getCommandClass() {
        return commandClass;
    }

    @Override
    public int getCommand() {
        return command;
    }
}
