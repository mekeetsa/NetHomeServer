package nu.nethome.home.items.zwave.messages;

import nu.nethome.home.items.zwave.Hex;
import nu.nethome.home.items.zwave.messages.commands.SwitchBinary;
import org.junit.Test;

/**
 *
 */
public class ApplicationCommandRequestTest {

    @Test
    public void binarySwitch() throws Exception {
        ApplicationCommandRequest request = new ApplicationCommandRequest((byte) 2, SwitchBinary.doSwitch(false));
        String result = Hex.asHexString(request.encode());
        System.out.println("event,ZWave_Message,Direction,Out,Value," + result);
        request = new ApplicationCommandRequest((byte) 2, SwitchBinary.report());
        result = Hex.asHexString(request.encode());
        System.out.println("event,ZWave_Message,Direction,Out,Value," + result);
    }
}
