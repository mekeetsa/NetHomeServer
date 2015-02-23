package nu.nethome.home.items.net.wemo;

import nu.nethome.home.item.AutoCreationInfo;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;

/**
 * Represents a Belkin wemo switch
 *
 * @author Stefan
 */

@Plugin
@HomeItemType(value = "Lamps", creationInfo = WemoSwitch.WemoCreationInfo.class)
public class WemoSwitch extends HomeItemAdapter implements HomeItem {

    public static final String UPN_P_CREATION_MESSAGE = "UPnP_Creation_Message";

    public static class WemoCreationInfo implements AutoCreationInfo {
        static final String[] CREATION_EVENTS = {UPN_P_CREATION_MESSAGE};
        @Override
        public String[] getCreationEvents() {
            return CREATION_EVENTS;
        }

        @Override
        public boolean canBeCreatedBy(Event e) {
            return e.getAttribute("DeviceType").equals("urn:Belkin:device:controllee:1");
        }

        @Override
        public String getCreationIdentification(Event e) {
            return String.format("Belkin Wemo Switch: \"%s\", SerialNr: %s",e.getAttribute("FriendlyName"), e.getAttribute("SerialNumber"));
        }
    }

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"WemoInsightSwitch\" Category=\"Lamps\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"DeviceURL\" Type=\"String\" Get=\"getDeviceURL\" 	Set=\"setDeviceURL\" />"
            + "  <Attribute Name=\"SerialNumber\" Type=\"String\" Get=\"getSerialNumber\" 	Init=\"setSerialNumber\" />"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(WemoSwitch.class.getName());
    private WemoInsightSwitchClient insightSwitch = new WemoInsightSwitchClient("http://192.168.1.16:49153");
    private String wemoDescriptionUrl = "";

    // Public attributes
    private String serialNumber = "";

    public String getModel() {
        return MODEL;
    }

    public boolean receiveEvent(Event event) {
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(UPN_P_CREATION_MESSAGE) &&
                event.getAttribute("DeviceType").equals("urn:Belkin:device:controllee:1") &&
                event.getAttribute("SerialNumber").equals(serialNumber)) {
            setDeviceURL(event.getAttribute("Location"));
            return true;
        }
        return handleInit(event);
    }

    @Override
    protected boolean initAttributes(Event event) {
        setDeviceURL(event.getAttribute("Location"));
        serialNumber = event.getAttribute("SerialNumber");
        return true;
    }

    private String extractBaseUrl(String url) {
        int pos = url.indexOf("/", 9);
        if (pos > 0) {
            return url.substring(0, pos);
        }
        return url;
    }

    public String getState() {
        try {
            return insightSwitch.getOnState() ? "On" : "Off";
        } catch (WemoException e) {
            return "";
        }
    }

    public String getDeviceURL() {
        return wemoDescriptionUrl;
    }

    public void setDeviceURL(String url) {
        wemoDescriptionUrl = url;
        insightSwitch.setWemoURL(extractBaseUrl(url));
    }

    public void on() {
        logger.fine("Switching on " + name);
        setOnState(true);
    }

    public void off() {
        logger.fine("Switching off " + name);
        setOnState(false);
    }

    private void setOnState(boolean isOn) {
        try {
            insightSwitch.setOnState(isOn);
        } catch (WemoException e) {
            logger.warning("Failed to contact Wemo device: " + e.getMessage());
        }
    }

    public void toggle() {
        try {
            setOnState(!insightSwitch.getOnState());
        } catch (WemoException e) {
            logger.warning("Failed to contact Wemo device: " + e.getMessage());
        }
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
}
