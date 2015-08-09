package nu.nethome.home.items.zwave.messages.commands;

import nu.nethome.home.items.zwave.messages.DecoderException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public abstract class Command {
    public final int commandClass;
    public final int command;

    public byte[] encode() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        addCommandData(result);
        return result.toByteArray();
    }

    protected void addCommandData(ByteArrayOutputStream result) {
        result.write(commandClass);
        result.write(command);
    }

    protected Command(int commandClass, int command) {
        this.commandClass = commandClass;
        this.command = command;
    }

    protected void decode(ByteArrayInputStream data) throws DecoderException {
        DecoderException.assertTrue(data.read() == commandClass, "Wrong command class in Association");
        DecoderException.assertTrue(data.read() == command, "Wrong command class in Association");
    }
}
