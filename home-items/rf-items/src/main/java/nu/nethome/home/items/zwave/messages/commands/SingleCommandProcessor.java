package nu.nethome.home.items.zwave.messages.commands;

import nu.nethome.home.items.zwave.messages.DecoderException;

abstract class SingleCommandProcessor {
    public abstract void process(byte[] command) throws DecoderException;
}
