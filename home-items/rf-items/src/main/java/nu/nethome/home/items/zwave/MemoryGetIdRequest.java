package nu.nethome.home.items.zwave;

/**
 *
 */
public class MemoryGetIdRequest extends Request {

    public static final byte MemoryGetId = (byte) 0x20;

    public MemoryGetIdRequest() {
        super(MemoryGetId);
    }
}
