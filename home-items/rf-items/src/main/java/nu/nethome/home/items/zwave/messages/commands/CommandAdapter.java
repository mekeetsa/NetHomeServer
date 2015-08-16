package nu.nethome.home.items.zwave.messages.commands;

import nu.nethome.home.items.zwave.messages.DecoderException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public abstract class CommandAdapter implements Command {
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

    protected CommandAdapter(int commandClass, int command) {
        this.commandClass = commandClass;
        this.command = command;
    }

    protected CommandAdapter(byte[] commandData) throws DecoderException {
        in = new ByteArrayInputStream(commandData);
        commandClass = in.read();
        command = in.read();
    }

    protected CommandAdapter(byte[] commandData, int commandClass, int command) throws DecoderException {
        this(commandData);
        DecoderException.assertTrue(this.commandClass == commandClass, "Wrong command class in Association");
        DecoderException.assertTrue(this.command == command, "Wrong command class in Association");
    }

    @Override
    public int getCommandClass() {
        return commandClass;
    }

    @Override
    public int getCommand() {
        return command;
    }

    public static CommandCode decodeCommandCode(byte[] message) throws DecoderException {
        if (message != null || message.length < 2){
            throw new DecoderException("Invalid command buffer");
        }
        return new CommandCode(((int)(message[0])) & 0xFF, ((int)(message[1])) & 0xFF);
    }
}
