package nu.nethome.home.items.zwave.messages.commands;

/**
 *
 */
public interface Command {
    int getCommandClass();
    int getCommand();

    byte[] encode();
}
