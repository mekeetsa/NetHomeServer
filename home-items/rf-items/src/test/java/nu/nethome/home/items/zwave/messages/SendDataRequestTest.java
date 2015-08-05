package nu.nethome.home.items.zwave.messages;

import nu.nethome.home.items.zwave.Hex;
import nu.nethome.home.items.zwave.messages.commands.Association;
import nu.nethome.home.items.zwave.messages.commands.SwitchBinary;
import org.junit.Test;

/**
 *
 */
public class SendDataRequestTest {

    public static final int TRANSMIT_OPTIONS = SendDataRequest.TRANSMIT_OPTION_ACK |
            SendDataRequest.TRANSMIT_OPTION_AUTO_ROUTE | SendDataRequest.TRANSMIT_OPTION_EXPLORE;

    @Test
    public void binarySwitch() throws Exception {
        SendDataRequest request = new SendDataRequest((byte) 2, SwitchBinary.doSwitch(true), TRANSMIT_OPTIONS);
        String result = Hex.asHexString(request.encode());
        System.out.println("event,ZWave_Message,Direction,Out,Value," + result);
        request = new SendDataRequest((byte) 2, SwitchBinary.report(), TRANSMIT_OPTIONS);
        result = Hex.asHexString(request.encode());
        System.out.println("event,ZWave_Message,Direction,Out,Value," + result);
        request = new SendDataRequest((byte) 2, Association.reportAssociations(), TRANSMIT_OPTIONS);
        result = Hex.asHexString(request.encode());
        System.out.println("event,ZWave_Message,Direction,Out,Value," + result);
    }

}
