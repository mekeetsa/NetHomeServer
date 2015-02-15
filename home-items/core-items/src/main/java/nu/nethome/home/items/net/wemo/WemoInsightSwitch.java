package nu.nethome.home.items.net.wemo;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;

/**
 * Represents a Belkin wemo insight switch
 * <p/>
 * <br>
 *
 * @author Stefan
 */
@Plugin
@HomeItemType(value = "Lamps", creationEvents = "UPnP_urn:Belkin:device:insight:1_Message")
public class WemoInsightSwitch extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"WemoInsightSwitch\" Category=\"Lamps\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"DeviceURL\" Type=\"String\" Get=\"getDeviceURL\" 	Set=\"setDeviceURL\" />"
            + "  <Attribute Name=\"SerialNumber\" Type=\"String\" Get=\"getSerialNumber\" 	Set=\"setSerialNumber\" />"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(WemoInsightSwitch.class.getName());
    private WemoInsightSwitchClient insightSwitch = new WemoInsightSwitchClient("http://192.168.1.16:49153");

    // Public attributes
    private boolean state = false;
    private String serialNumber = "";

    public String getModel() {
        return MODEL;
    }

    public boolean receiveEvent(Event event) {
        return handleInit(event);
    }

    @Override
    protected boolean initAttributes(Event event) {
        insightSwitch.setWemoURL(extractBaseUrl(event.getAttribute("Location")));
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
        if (state) {
            return "On";
        }
        return "Off";
    }

    public String getDeviceURL() {
        return insightSwitch.getWemoURL();
    }

    public void setDeviceURL(String url) {
        insightSwitch.setWemoURL(url);
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
        state = isOn;
    }

    public void toggle() {
        logger.fine("Toggling " + name);
        state = !state;
        if (state) {
            on();
        } else {
            off();
        }
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
}
