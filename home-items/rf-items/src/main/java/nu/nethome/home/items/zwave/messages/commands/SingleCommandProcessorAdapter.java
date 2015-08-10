package nu.nethome.home.items.zwave.messages.commands;

import nu.nethome.home.items.zwave.messages.DecoderException;

abstract class SingleCommandProcessorAdapter<T>  extends SingleCommandProcessor {
    protected void process(T command) throws DecoderException {}
}
