package nu.nethome.home.items.zwave.messages.commands;

import nu.nethome.home.items.zwave.messages.DecoderException;

import java.util.HashMap;
import java.util.Map;

public class MultiCommandProcessor implements CommandProcessor {

    private Map<CommandCode, CommandProcessor> processors = new HashMap<>();
    private CommandProcessor defaultProcessor;

    public MultiCommandProcessor() {
        this.defaultProcessor = new UndecodedCommand.Processor();
    }

    public MultiCommandProcessor(CommandProcessor defaultProcessor) {
        this.defaultProcessor = defaultProcessor;
    }

    @Override
    public Command process(byte[] message, int node) throws DecoderException {
        CommandProcessor processor = processors.get(CommandAdapter.decodeCommandCode(message));
        if (processor != null) {
            return processor.process(message, node);
        }
        return defaultProcessor.process(message, node);
    }

    public void addCommandProcessor(CommandCode command, CommandProcessor processor) {
        processors.put(command, processor);
    }

    public void setDefaultProcessor(CommandProcessor defaultProcessor) {
        this.defaultProcessor = defaultProcessor;
    }
}
