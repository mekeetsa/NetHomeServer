package nu.nethome.home.items.zwave.messages.commands;

import nu.nethome.home.items.zwave.messages.DecoderException;

abstract class CommandProcessorAdapter<T>  implements CommandProcessor {
    protected T process(T command) throws DecoderException {return command;}
}
