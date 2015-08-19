package nu.nethome.home.items.zwave.messages;

/**
 *
 */
public abstract class MessageProcessorAdaptor<T> implements MessageProcessor {
    protected T process(T command) throws DecoderException {return command;}
}
