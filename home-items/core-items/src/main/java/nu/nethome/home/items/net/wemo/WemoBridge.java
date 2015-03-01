package nu.nethome.home.items.net.wemo;

import nu.nethome.home.item.AutoCreationInfo;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;

/**
 * Represents a Belkin wemo bridge device
 *
 * @author Stefan
 */

@Plugin
@HomeItemType(value = "Hardware", creationInfo = WemoBridge.WemoCreationInfo.class)
public class WemoBridge extends HomeItemAdapter implements HomeItem {

    public static final String UPN_P_CREATION_MESSAGE = "UPnP_Creation_Message";
    public static final String BELKIN_WEMO_BRIDGE_DEVICE = "urn:Belkin:device:bridge:1";

    public static class WemoCreationInfo implements AutoCreationInfo {
        static final String[] CREATION_EVENTS = {UPN_P_CREATION_MESSAGE};
        @Override
        public String[] getCreationEvents() {
            return CREATION_EVENTS;
        }

        @Override
        public boolean canBeCreatedBy(Event e) {
            return e.getAttribute("DeviceType").equals(BELKIN_WEMO_BRIDGE_DEVICE);
        }

        @Override
        public String getCreationIdentification(Event e) {
            return String.format("Belkin Wemo Bridge: \"%s\", UDN: %s",e.getAttribute("FriendlyName"), e.getAttribute("UDN"));
        }
    }

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"WemoBridge\" Category=\"Hardware\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"DeviceURL\" Type=\"String\" Get=\"getDeviceURL\" 	Set=\"setDeviceURL\" />"
            + "  <Attribute Name=\"UDN\" Type=\"String\" Get=\"getUDN\" 	Init=\"setUDN\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(WemoBridge.class.getName());
    private String wemoDescriptionUrl = "";
    private String udn = "";

    public String getModel() {
        return MODEL;
    }

    public boolean receiveEvent(Event event) {
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(UPN_P_CREATION_MESSAGE) &&
                event.getAttribute("DeviceType").equals(BELKIN_WEMO_BRIDGE_DEVICE) &&
                event.getAttribute("UDN").equals(udn)) {
            setDeviceURL(event.getAttribute("Location"));
            return true;
        }
        return handleInit(event);
    }

    @Override
    protected boolean initAttributes(Event event) {
        setDeviceURL(event.getAttribute("Location"));
        udn = event.getAttribute("UDN");
        return true;
    }

    private String extractBaseUrl(String url) {
        int pos = url.indexOf("/", 9);
        if (pos > 0) {
            return url.substring(0, pos);
        }
        return url;
    }

    public String getDeviceURL() {
        return wemoDescriptionUrl;
    }

    public void setDeviceURL(String url) {
        wemoDescriptionUrl = url;
    }

    public String getUDN() {
        return udn;
    }

    public void setUDN(String udn) {
        this.udn = udn;
    }
}
