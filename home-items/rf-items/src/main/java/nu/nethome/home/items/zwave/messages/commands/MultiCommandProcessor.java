package nu.nethome.home.items.zwave.messages.commands;

import nu.nethome.home.items.zwave.messages.DecoderException;

import java.util.HashMap;
import java.util.Map;

public class MultiCommandProcessor implements CommandProcessor {

    Map<CommandCode, CommandProcessor> processors = new HashMap<>();

    @Override
    public Command process(byte[] message) throws DecoderException {
        CommandProcessor processor = processors.get(CommandAdapter.decodeCommandCode(message));
        if (processor != null) {
            return processor.process(message);
        }
        return null;
    }

    public void addCommandProcessor(CommandCode command, CommandProcessor processor) {
        processors.put(command, processor);
    }
}
