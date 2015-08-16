package nu.nethome.home.items.zwave.messages;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class MultiMessageProcessor implements MessageProcessor {

    Map<Integer, MessageProcessor> processors = new HashMap<>();

    @Override
    public Message process(byte[] message) throws DecoderException, IOException {
        MessageProcessor processor = processors.get(MessageAdaptor.decodeMessageId(message));
        if (processor != null) {
            return processor.process(message);
        }
        return null;
    }

    public void addMessageProcessor(int messageId, MessageProcessor processor) {
        processors.put(messageId, processor);
    }
}
