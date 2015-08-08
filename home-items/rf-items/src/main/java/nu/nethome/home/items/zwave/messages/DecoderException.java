package nu.nethome.home.items.zwave.messages;

/**
*
*/
public class DecoderException extends Throwable {
    public DecoderException(String message) {
        super(message);
    }

    public static void assertTrue(boolean value, String message) throws DecoderException {
        if (!value) {
            throw new DecoderException(message);
        }
    }
}
