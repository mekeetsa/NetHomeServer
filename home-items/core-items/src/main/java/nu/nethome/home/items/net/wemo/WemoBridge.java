package nu.nethome.home.items.net.wemo;

import nu.nethome.home.item.AutoCreationInfo;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.util.List;
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
    public static final String WEMO_LIGHT_MESSAGE = "WemoLight_Message";
    public static final String DEVICE_INDEX = "DeviceIndex";
    public static final String DEVICE_ID = "DeviceID";
    public static final String FRIENDLY_NAME = "FriendlyName";
    public static final String FIRMWARE_VERSION = "FirmwareVersion";
    public static final String CAPABILITY_IDS = "CapabilityIDs";
    public static final String ON_STATE = "OnState";
    public static final String BRIGHTNESS = "Brightness";
    public static final String BRIDGE_URL = "BridgeUrl";
    public static final String BRIDGE_UDN = "BridgeUDN";

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
            + "  <Attribute Name=\"DeviceURL\" Type=\"String\" Get=\"getDeviceURL\" Set=\"setDeviceURL\" />"
            + "  <Attribute Name=\"Identity\" Type=\"String\" Get=\"getUDN\" Init=\"setUDN\" />"
            + "  <Attribute Name=\"ConnectedLamps\" Type=\"String\" Get=\"getConnectedLamps\" />"
            + "  <Action Name=\"ReportDevices\" Method=\"reportAllDevices\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(WemoBridge.class.getName());
    private String wemoDescriptionUrl = "";
    private String udn = "";
    private WemoBridgeSoapClient soapClient;
    private int connectedLamps = -1;

    public WemoBridge() {
        soapClient = new WemoBridgeSoapClient("");
    }

    WemoBridgeSoapClient getSoapClient() {
        return soapClient;
    }

    public String getModel() {
        return MODEL;
    }

    @Override
    public void activate() {
        reportAllDevices();
    }

    public boolean receiveEvent(Event event) {
        if (event.isType(UPN_P_CREATION_MESSAGE) &&
                event.getAttribute("DeviceType").equals(BELKIN_WEMO_BRIDGE_DEVICE) &&
                event.getAttribute("UDN").equals(udn)) {
            setDeviceURL(event.getAttribute("Location"));
            return true;
        }  else if (event.isType("ReportItems")) {
            reportAllDevices();
            return true;
        } else if (event.isType(WEMO_LIGHT_MESSAGE) &&
                event.getAttribute("Direction").equals("Out")) {
            return updateDeviceState(event);
        }
        return handleInit(event);
    }

    private boolean updateDeviceState(Event event) {
        boolean isOn = event.getAttributeInt(ON_STATE) == 1;
        int brightness = event.getAttributeInt(BRIGHTNESS);
        try {
            return soapClient.setDeviceStatus(event.getAttribute(DEVICE_ID), isOn, brightness);
        } catch (WemoException e) {
            logger.warning("Failed to send message to Wemo bridge");
            return false;
        }
    }

    @Override
    protected boolean initAttributes(Event event) {
        setDeviceURL(event.getAttribute("Location"));
        udn = event.getAttribute("UDN");
        return true;
    }

    public String reportAllDevices() {
        try {
            List<BridgeDevice> endDevices = getSoapClient().getEndDevices(udn);
            connectedLamps = endDevices.size();
            for (BridgeDevice device : endDevices) {
                reportDevice(device);
            }
        } catch (WemoException e) {
            logger.warning("Failed to connect to WeMo Bridge: " + e.getMessage());
        }
        return "";
    }

    private void reportDevice(BridgeDevice device) {
        Event event = server.createEvent(WEMO_LIGHT_MESSAGE, "");
        event.setAttribute(DEVICE_INDEX, device.getDeviceIndex());
        event.setAttribute(DEVICE_ID, device.getDeviceID());
        event.setAttribute(FRIENDLY_NAME, device.getFriendlyName());
        event.setAttribute(FIRMWARE_VERSION, device.getFirmwareVersion());
        event.setAttribute(CAPABILITY_IDS, device.getCapabilityIDs());
        event.setAttribute(ON_STATE, device.getOnState());
        event.setAttribute(BRIGHTNESS, device.getBrightness());
        event.setAttribute(BRIDGE_URL, wemoDescriptionUrl);
        event.setAttribute(BRIDGE_UDN, udn);
        event.setAttribute("Direction", "In");
        server.send(event);
    }

    public String getDeviceURL() {
        return wemoDescriptionUrl;
    }

    public void setDeviceURL(String url) {
        wemoDescriptionUrl = url;
        getSoapClient().setWemoURL(extractBaseUrl(url));
    }

    private String extractBaseUrl(String url) {
        int pos = url.indexOf("/", 9);
        if (pos > 0) {
            return url.substring(0, pos);
        }
        return url;
    }

    public String getUDN() {
        return udn;
    }

    public void setUDN(String udn) {
        this.udn = udn;
    }

    public String getConnectedLamps() {
        return connectedLamps > 0 ? Integer.toString(connectedLamps) : "";
    }
}
