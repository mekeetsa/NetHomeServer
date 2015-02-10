package nu.nethome.home.items.net.wemo.soap;

import javax.xml.soap.SOAPException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WemoInsightSwitchClient extends LightSoapClient {
    public static final String BASICEVENT1_SERVICE_URL = "/upnp/control/basicevent1";
    public static final String BASICEVENT_NAMESPACE = "urn:Belkin:service:basicevent:1";
    public static final String SET_BINARY_STATE = "SetBinaryState";

    private String wemoURL;

    public WemoInsightSwitchClient(String wemoURL) {
        this.wemoURL = wemoURL;
    }

    public void setOnState(boolean isOn) throws WemoException {
        Map<String, String> args =  new HashMap<>();
        args.put("BinaryState", isOn ? "1" :  "0");
        try {
            sendRequest(BASICEVENT_NAMESPACE, wemoURL + BASICEVENT1_SERVICE_URL, SET_BINARY_STATE, args);
        } catch (SOAPException|IOException e) {
            throw new WemoException(e);
        }
    }

    public static class WemoException extends Throwable {
        public WemoException(Exception e) {
            super(e);
        }
    }

    public String getWemoURL() {
        return wemoURL;
    }

    public void setWemoURL(String wemoURL) {
        this.wemoURL = wemoURL;
    }
}
