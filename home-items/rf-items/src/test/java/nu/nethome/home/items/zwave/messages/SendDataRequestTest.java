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
        request = new SendDataRequest((byte) 6, Association.getAssociation(2), TRANSMIT_OPTIONS);
        result = Hex.asHexString(request.encode());
        System.out.println("event,ZWave_Message,Direction,Out,Value," + result);
        //
        // 00 04 0006 06: 85 03 01 0A 00 01
        // 00 04 0006 06: 85 03 02 0A 00 02
        // 00 04 0006 0F: 8F 01 02 05 85 03 03 0A 00 05 85 03 03 0A 00 + 00 04 0006 03: 80 03 64
        // 00 04 0006 05: 85 03 04 0A 00 (4)
        // 00 04 0006 06: 85 03 02 0A 00 02 (2)


    }

}
