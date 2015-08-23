package nu.nethome.home.items.zwave.messages.commands;

import nu.nethome.home.items.zwave.messages.DecoderException;

public interface CommandProcessor {
    Command process(byte[] command, int node) throws DecoderException;
}
