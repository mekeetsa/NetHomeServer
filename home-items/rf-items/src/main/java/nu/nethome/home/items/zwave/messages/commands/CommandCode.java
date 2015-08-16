package nu.nethome.home.items.zwave.messages.commands;

/**
 *
 */
public class CommandCode {
    public final int commandClass;
    public final int command;

    public CommandCode(int commandClass, int command) {
        this.commandClass = commandClass;
        this.command = command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommandCode that = (CommandCode) o;

        if (command != that.command) return false;
        if (commandClass != that.commandClass) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = commandClass;
        result = 31 * result + command;
        return result;
    }
}
