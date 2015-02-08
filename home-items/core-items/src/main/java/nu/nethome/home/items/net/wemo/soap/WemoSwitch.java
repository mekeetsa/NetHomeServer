package nu.nethome.home.items.net.wemo.soap;

import javax.xml.soap.SOAPException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class WemoSwitch extends LightSoapClient {

    public void setOn(boolean isOn) throws SOAPException, IOException {
        Map<String, String> args =  new HashMap<>();
        args.put("BinaryState", isOn ? "1" :  "0");
        sendRequest("urn:Belkin:service:basicevent:1", "http://192.168.1.16:49153/upnp/control/basicevent1", "SetBinaryState", args);
        //sendRequest("urn:Belkin:service:basicevent:1", "http://127.0.0.1:49000/upnp/control/basicevent1", "SetBinaryState", args);
    }
}
