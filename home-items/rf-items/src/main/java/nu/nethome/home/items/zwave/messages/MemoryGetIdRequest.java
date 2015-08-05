package nu.nethome.home.items.zwave.messages;

/*
 * event,ZWave_Message,Direction,Out,Value,0020
 */
public class MemoryGetIdRequest extends Request {

    public static final byte MemoryGetId = (byte) 0x20;

    public MemoryGetIdRequest() {
        super(MemoryGetId);
    }
}
