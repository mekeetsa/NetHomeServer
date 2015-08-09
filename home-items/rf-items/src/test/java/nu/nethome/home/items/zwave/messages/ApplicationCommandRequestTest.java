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
        ApplicationCommand.Request request = new ApplicationCommand.Request((byte) 2, new SwitchBinary.Set(false));
        String result = Hex.asHexString(request.encode());
        System.out.println("event,ZWave_Message,Direction,Out,Value," + result);
        request = new ApplicationCommand.Request((byte) 2, new SwitchBinary.Get());
        result = Hex.asHexString(request.encode());
        System.out.println("event,ZWave_Message,Direction,Out,Value," + result);
    }
}
