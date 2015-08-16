package nu.nethome.home.items.zwave.messages;

import java.io.IOException;

/**
 *
 */
interface MessageProcessor {
    Message process(byte[] message) throws DecoderException, IOException;
}
